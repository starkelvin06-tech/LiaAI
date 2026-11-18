package com.Lia.assistant.voice

import android.content.Context
import com.Lia.assistant.action.ActionExecutor
import com.Lia.assistant.action.InstalledAppLabelCache
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

/**
 * Singleton orchestrator for full-duplex real-time voice sessions.
 * Independent of Activity lifecycle; keeps running in background via VoiceForegroundService.
 */
object VoiceSessionManager {

    private val _state = MutableStateFlow(NovaOrbState.IDLE)
    val state: StateFlow<NovaOrbState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private val _statusText = MutableStateFlow("Tap to start")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _currentPersonality = MutableStateFlow(Personality.DEFAULT)
    val currentPersonality: StateFlow<Personality> = _currentPersonality.asStateFlow()

    private val _currentLanguage = MutableStateFlow(LanguagePreference.AUTO)
    val currentLanguage: StateFlow<LanguagePreference> = _currentLanguage.asStateFlow()

    private val _detectedLanguage = MutableStateFlow(LanguageDetector.Result.UNKNOWN)
    val detectedLanguage: StateFlow<LanguageDetector.Result> = _detectedLanguage.asStateFlow()

    private val _vadActive = MutableStateFlow(false)
    val vadActive: StateFlow<Boolean> = _vadActive.asStateFlow()

    val latencyTracker = VoiceLatencyTracker()
    val memory = ConversationMemory()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessionMutex = Mutex()
    private val sessionEpoch = AtomicInteger(0)

    private var liveClient: GeminiLiveClient? = null
    private var audioIO: LiveAudioIO? = null

    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0
    private const val MAX_RECONNECT_ATTEMPTS = 5

    private var playbackSilenceJob: Job? = null
    private const val SILENCE_TIMEOUT_MS = 300L

    fun start(context: Context) {
        if (_isActive.value) return
        _isActive.value = true
        _lastError.value = null
        reconnectAttempts = 0
        latencyTracker.resetReconnect()
        memory.clear()
        EchoGuard.reset()

        // Pre-warm app cache in background
        scope.launch {
            try {
                InstalledAppLabelCache.prewarm(context)
            } catch (_: Exception) {}
        }

        // Initialize Audio IO
        audioIO = LiveAudioIO(
            onMicChunk = { bytes ->
                if (!_muted.value && !EchoGuard.isMicMuted()) {
                    liveClient?.sendAudio(bytes)
                }
            },
            onMicAmplitude = { amp ->
                if (!EchoGuard.isMicMuted()) {
                    _amplitude.value = amp
                    _vadActive.value = amp > 0.05f
                }
            }
        )

        scope.launch {
            sessionMutex.withLock {
                connectSession(context.applicationContext, isResuming = false)
            }
        }
    }

    private suspend fun connectSession(context: Context, isResuming: Boolean) {
        val epoch = sessionEpoch.incrementAndGet()

        liveClient?.disconnect()
        liveClient = null

        val apiKey = ApiKeyStore.getKey(context)
        if (apiKey.isNullOrBlank()) {
            _state.value = NovaOrbState.ERROR
            _lastError.value = "Gemini API key missing. Enter it in Settings."
            _statusText.value = "Missing API Key"
            _isActive.value = false
            return
        }

        val personality = PersonalityRepository.getPersonality(context)
        val language = PersonalityRepository.getLanguage(context)
        val assistantName = PersonalityRepository.getAssistantName(context)
        val voiceName = PersonalityRepository.getVoiceName(context)

        _currentPersonality.value = personality
        _currentLanguage.value = language
        _state.value = NovaOrbState.CONNECTING
        _statusText.value = if (isResuming) "Reconnecting..." else "Connecting to Lia..."

        val recapText = if (isResuming) memory.recap() else null
        val systemPrompt = PersonalityPromptBuilder.build(
            personality = personality,
            language = language,
            detectedLanguage = _detectedLanguage.value,
            recap = recapText,
            assistantName = assistantName
        )

        val client = GeminiLiveClient(object : GeminiLiveClient.Listener {
            override fun onSetupComplete() {
                if (sessionEpoch.get() != epoch) return
                _state.value = NovaOrbState.LISTENING
                _statusText.value = "Listening..."
                audioIO?.startMic()
                reconnectAttempts = 0
            }

            override fun onAudioChunk(bytes: ByteArray) {
                if (sessionEpoch.get() != epoch) return

                if (_state.value != NovaOrbState.SPEAKING) {
                    EchoGuard.notifyPlaybackStarted()
                    latencyTracker.markFirstAudio()
                }

                _state.value = NovaOrbState.SPEAKING
                _statusText.value = "Speaking..."
                val rms = audioIO?.playChunk(bytes) ?: 0f
                _amplitude.value = rms

                // Re-arm 300 ms playback silence watchdog
                playbackSilenceJob?.cancel()
                playbackSilenceJob = scope.launch {
                    delay(SILENCE_TIMEOUT_MS)
                    if (sessionEpoch.get() == epoch && _state.value == NovaOrbState.SPEAKING) {
                        EchoGuard.notifyPlaybackStopped()
                        _state.value = NovaOrbState.LISTENING
                        _statusText.value = "Listening..."
                    }
                }
            }

            override fun onInputTranscript(text: String) {
                if (sessionEpoch.get() != epoch) return
                memory.addUserTurn(text)
                _detectedLanguage.value = LanguageDetector.detect(text)
                latencyTracker.markUserSpeechEnd()
            }

            override fun onOutputTranscript(text: String) {
                if (sessionEpoch.get() != epoch) return
                memory.addAssistantTurn(text)
            }

            override fun onToolCall(id: String, name: String, args: JSONObject) {
                if (sessionEpoch.get() != epoch) return
                scope.launch {
                    val result = if (name == "build_website") {
                        JSONObject().put("result", "forge_started")
                    } else {
                        ActionExecutor.execute(context, name, args)
                    }
                    if (sessionEpoch.get() == epoch) {
                        liveClient?.sendToolResponse(id, name, result)
                    }
                }
            }

            override fun onTurnComplete() {
                if (sessionEpoch.get() != epoch) return
                playbackSilenceJob?.cancel()
                EchoGuard.notifyPlaybackStopped()
                _state.value = NovaOrbState.LISTENING
                _statusText.value = "Listening..."
            }

            override fun onInterrupted() {
                if (sessionEpoch.get() != epoch) return
                playbackSilenceJob?.cancel()
                audioIO?.flushPlayback()
                EchoGuard.notifyPlaybackStopped()
                _state.value = NovaOrbState.LISTENING
                _statusText.value = "Listening..."
            }

            override fun onError(message: String) {
                if (sessionEpoch.get() != epoch) return
                if (_isActive.value) {
                    scheduleReconnect(context)
                }
            }

            override fun onClosed() {
                if (sessionEpoch.get() != epoch) return
                if (_isActive.value) {
                    scheduleReconnect(context)
                }
            }
        })

        liveClient = client
        val mappedGeminiVoice = mapVoiceToGemini(voiceName)
        client.connect(apiKey, systemPrompt, mappedGeminiVoice)
    }

    private fun mapVoiceToGemini(name: String): String {
        return when (name.lowercase()) {
            "puck" -> "Puck"
            "charon" -> "Charon"
            "kore" -> "Kore"
            "fenrir" -> "Fenrir"
            else -> "Aoede"
        }
    }

    private fun scheduleReconnect(context: Context) {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            sessionMutex.withLock {
                if (!_isActive.value) return@withLock

                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    _state.value = NovaOrbState.ERROR
                    _statusText.value = "Connection lost - tap Stop and reopen Voice Mode"
                    _lastError.value = "Maximum reconnect attempts reached."
                    return@withLock
                }

                reconnectAttempts++
                latencyTracker.incrementReconnect()
                _statusText.value = "Reconnecting (attempt $reconnectAttempts)..."
                delay(1200)

                if (_isActive.value) {
                    connectSession(context, isResuming = true)
                }
            }
        }
    }

    fun refreshInstructions(context: Context) {
        if (!_isActive.value) return
        scope.launch {
            sessionMutex.withLock {
                if (_isActive.value) {
                    connectSession(context.applicationContext, isResuming = true)
                }
            }
        }
    }

    fun toggleMute() {
        _muted.value = !_muted.value
    }

    fun stop() {
        sessionEpoch.incrementAndGet()
        reconnectJob?.cancel()
        reconnectJob = null
        playbackSilenceJob?.cancel()
        playbackSilenceJob = null

        liveClient?.disconnect()
        liveClient = null

        audioIO?.release()
        audioIO = null

        EchoGuard.reset()
        memory.clear()

        _isActive.value = false
        _state.value = NovaOrbState.IDLE
        _statusText.value = "Tap to start"
        _amplitude.value = 0f
        _muted.value = false
        _vadActive.value = false
    }
}
