package com.Lia.assistant.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * EchoGuard prevents Lia from picking up her own voice from the loudspeaker.
 * Uses a reference-counted active playback tracker with a 450 ms decay tail and 60 s safety watchdog.
 */
object EchoGuard {
    const val ECHO_TAIL_MS = 450L
    const val WATCHDOG_MS = 60_000L

    private val activePlaybacks = AtomicInteger(0)
    private val muted = AtomicBoolean(false)

    // Internal scope for delays
    var scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var tailJob: Job? = null
    private var watchdogJob: Job? = null
    private val lock = Any()

    fun notifyPlaybackStarted() {
        synchronized(lock) {
            val count = activePlaybacks.incrementAndGet()
            tailJob?.cancel()
            tailJob = null
            muted.set(true)

            // Re-arm watchdog
            watchdogJob?.cancel()
            watchdogJob = scope.launch {
                delay(WATCHDOG_MS)
                synchronized(lock) {
                    activePlaybacks.set(0)
                    muted.set(false)
                }
            }
        }
    }

    fun notifyPlaybackStopped() {
        synchronized(lock) {
            val count = activePlaybacks.decrementAndGet()
            if (count <= 0) {
                activePlaybacks.set(0)
                tailJob?.cancel()
                tailJob = scope.launch {
                    delay(ECHO_TAIL_MS)
                    synchronized(lock) {
                        muted.set(false)
                        watchdogJob?.cancel()
                        watchdogJob = null
                    }
                }
            }
        }
    }

    fun isMicMuted(): Boolean {
        return muted.get()
    }

    fun reset() {
        synchronized(lock) {
            activePlaybacks.set(0)
            muted.set(false)
            tailJob?.cancel()
            tailJob = null
            watchdogJob?.cancel()
            watchdogJob = null
        }
    }
}
