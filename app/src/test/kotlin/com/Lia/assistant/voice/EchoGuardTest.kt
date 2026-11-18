package com.Lia.assistant.voice

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EchoGuardTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        EchoGuard.scope = testScope
        EchoGuard.reset()
    }

    @After
    fun tearDown() {
        EchoGuard.reset()
    }

    @Test
    fun testPlaybackStartedMutesMic() {
        assertFalse(EchoGuard.isMicMuted())
        EchoGuard.notifyPlaybackStarted()
        assertTrue(EchoGuard.isMicMuted())
    }

    @Test
    fun testCounterReferenceCounting() = testScope.runTest {
        // Start 2 concurrent chunks
        EchoGuard.notifyPlaybackStarted()
        EchoGuard.notifyPlaybackStarted()
        assertTrue(EchoGuard.isMicMuted())

        // Stop 1 chunk -> still playing 1 chunk
        EchoGuard.notifyPlaybackStopped()
        assertTrue(EchoGuard.isMicMuted())

        // Stop 2nd chunk -> now 0, enters 450ms tail
        EchoGuard.notifyPlaybackStopped()
        assertTrue("Should still be muted during tail window", EchoGuard.isMicMuted())

        // Advance 200ms -> still in tail window
        advanceTimeBy(200)
        assertTrue(EchoGuard.isMicMuted())

        // Advance past 450ms (300ms more -> 500ms total)
        advanceTimeBy(300)
        assertFalse("Should be unmuted after tail window", EchoGuard.isMicMuted())
    }

    @Test
    fun testWatchdogForceUnmutes() = testScope.runTest {
        // Start playback but never call notifyPlaybackStopped
        EchoGuard.notifyPlaybackStarted()
        assertTrue(EchoGuard.isMicMuted())

        // Advance 30 seconds -> still muted
        advanceTimeBy(30_000)
        assertTrue(EchoGuard.isMicMuted())

        // Advance past 60s watchdog
        advanceTimeBy(31_000)
        assertFalse("Watchdog should force-unmute after 60s", EchoGuard.isMicMuted())
    }

    @Test
    fun testReset() {
        EchoGuard.notifyPlaybackStarted()
        assertTrue(EchoGuard.isMicMuted())

        EchoGuard.reset()
        assertFalse(EchoGuard.isMicMuted())
    }
}
