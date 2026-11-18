package com.Lia.assistant.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceLatencyTrackerTest {

    @Test
    fun testInitialLatencyIsNull() {
        val tracker = VoiceLatencyTracker()
        assertNull(tracker.getLatencyMs())
    }

    @Test
    fun testInOrderMarksYieldValidDelta() {
        val tracker = VoiceLatencyTracker()
        tracker.markUserSpeechEnd()
        Thread.sleep(15)
        tracker.markFirstAudio()

        val latency = tracker.getLatencyMs()
        assertNotNull(latency)
        assertTrue("Latency delta should be >= 10 ms", latency!! >= 10)
    }

    @Test
    fun testOutOfOrderMarksIgnored() {
        val tracker = VoiceLatencyTracker()
        // First audio marked before user speech end
        tracker.markFirstAudio()
        assertNull("Out of order mark should result in null latency", tracker.getLatencyMs())

        tracker.markUserSpeechEnd()
        // Still null because firstAudio wasn't recorded after user speech end
        assertNull(tracker.getLatencyMs())
    }

    @Test
    fun testResetClearsTimestamps() {
        val tracker = VoiceLatencyTracker()
        tracker.markUserSpeechEnd()
        tracker.markFirstAudio()
        assertNotNull(tracker.getLatencyMs())

        tracker.reset()
        assertNull(tracker.getLatencyMs())
    }

    @Test
    fun testReconnectCounter() {
        val tracker = VoiceLatencyTracker()
        assertEquals(0, tracker.reconnectCount.value)

        tracker.incrementReconnect()
        tracker.incrementReconnect()
        assertEquals(2, tracker.reconnectCount.value)

        tracker.resetReconnect()
        assertEquals(0, tracker.reconnectCount.value)
    }
}
