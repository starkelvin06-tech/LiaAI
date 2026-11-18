package com.Lia.assistant.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceLatencyTracker {
    private enum class Mark {
        USER_SPEECH_END,
        FIRST_AUDIO
    }

    private val timestamps = mutableMapOf<Mark, Long>()
    private val lock = Any()

    private val _reconnectCount = MutableStateFlow(0)
    val reconnectCount: StateFlow<Int> = _reconnectCount.asStateFlow()

    fun markUserSpeechEnd() {
        synchronized(lock) {
            timestamps[Mark.USER_SPEECH_END] = System.currentTimeMillis()
        }
    }

    fun markFirstAudio() {
        synchronized(lock) {
            // Out-of-order marks ignored: only record if user speech ended first
            if (timestamps.containsKey(Mark.USER_SPEECH_END)) {
                timestamps[Mark.FIRST_AUDIO] = System.currentTimeMillis()
            }
        }
    }

    fun getLatencyMs(): Long? {
        synchronized(lock) {
            val userEnd = timestamps[Mark.USER_SPEECH_END] ?: return null
            val firstAudio = timestamps[Mark.FIRST_AUDIO] ?: return null
            return if (firstAudio >= userEnd) {
                firstAudio - userEnd
            } else {
                null
            }
        }
    }

    fun reset() {
        synchronized(lock) {
            timestamps.clear()
        }
    }

    fun incrementReconnect() {
        _reconnectCount.update { it + 1 }
    }

    fun resetReconnect() {
        _reconnectCount.update { 0 }
    }
}
