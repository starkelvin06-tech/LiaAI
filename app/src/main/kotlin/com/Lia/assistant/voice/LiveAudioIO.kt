package com.Lia.assistant.voice

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Handles low-latency 16 kHz AudioRecord microphone input and 24 kHz AudioTrack speaker output.
 * Attaches AEC, NS, and AGC when supported by hardware.
 */
class LiveAudioIO(
    private val onMicChunk: (ByteArray) -> Unit,
    private val onMicAmplitude: (Float) -> Unit
) {
    companion object {
        const val MIC_SAMPLE_RATE = 16000
        const val SPEAKER_SAMPLE_RATE = 24000
        const val MIC_CHUNK_BYTES = 2048
    }

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var micThread: Thread? = null
    private val isMicRunning = AtomicBoolean(false)

    // Hardware Audio Effects
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null

    private val trackLock = Any()

    @Synchronized
    fun startMic() {
        if (isMicRunning.get()) return

        val minBuf = AudioRecord.getMinBufferSize(
            MIC_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufSize = max(minBuf, 4096)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MIC_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize
            )

            val sessionId = record.audioSessionId
            try {
                if (AcousticEchoCanceler.isAvailable()) {
                    aec = AcousticEchoCanceler.create(sessionId)?.apply { enabled = true }
                }
            } catch (e: Exception) {}

            try {
                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(sessionId)?.apply { enabled = true }
                }
            } catch (e: Exception) {}

            try {
                if (AutomaticGainControl.isAvailable()) {
                    agc = AutomaticGainControl.create(sessionId)?.apply { enabled = true }
                }
            } catch (e: Exception) {}

            record.startRecording()
            audioRecord = record
            isMicRunning.set(true)

            micThread = Thread({
                val buffer = ByteArray(MIC_CHUNK_BYTES)
                while (isMicRunning.get()) {
                    val readBytes = record.read(buffer, 0, buffer.size)
                    if (readBytes > 0) {
                        val chunk = buffer.copyOf(readBytes)
                        val amp = rms(chunk)
                        onMicChunk(chunk)
                        onMicAmplitude(amp)
                    }
                }
            }, "LiaMicThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        } catch (e: Exception) {
            isMicRunning.set(false)
        }
    }

    @Synchronized
    fun stopMic() {
        isMicRunning.set(false)
        micThread?.interrupt()
        micThread = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {}
        audioRecord = null

        aec?.release(); aec = null
        ns?.release(); ns = null
        agc?.release(); agc = null
    }

    private fun getOrCreateTrack(): AudioTrack {
        synchronized(trackLock) {
            audioTrack?.let { return it }

            val minBuf = AudioTrack.getMinBufferSize(
                SPEAKER_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBuf * 4

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(SPEAKER_SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            val track = AudioTrack(
                attributes,
                format,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.play()
            audioTrack = track
            return track
        }
    }

    fun playChunk(bytes: ByteArray): Float {
        if (bytes.isEmpty()) return 0f
        val track = getOrCreateTrack()
        track.write(bytes, 0, bytes.size)
        return rms(bytes)
    }

    fun flushPlayback() {
        synchronized(trackLock) {
            audioTrack?.let { track ->
                try {
                    track.pause()
                    track.flush()
                    track.play()
                } catch (e: Exception) {}
            }
        }
    }

    fun setVolume(volume: Float) {
        synchronized(trackLock) {
            val clamped = volume.coerceIn(0f, 1f)
            audioTrack?.setVolume(clamped)
        }
    }

    fun release() {
        stopMic()
        synchronized(trackLock) {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {}
            audioTrack = null
        }
    }

    companion object Utils {
        /**
         * Calculates normalized RMS amplitude for 16-bit little-endian PCM samples.
         * Clamped in 0.0f..1.0f.
         */
        fun rms(pcm16Bytes: ByteArray): Float {
            if (pcm16Bytes.size < 2) return 0f
            var sum = 0.0
            val numSamples = pcm16Bytes.size / 2

            for (i in 0 until numSamples) {
                val idx = i * 2
                val low = pcm16Bytes[idx].toInt() and 0xFF
                val high = pcm16Bytes[idx + 1].toInt()
                val sample = (high shl 8) or low
                sum += (sample * sample).toDouble()
            }

            val mean = sum / numSamples
            val rmsVal = sqrt(mean) / 32768.0
            return min(1.0, max(0.0, rmsVal)).toFloat()
        }
    }
}
