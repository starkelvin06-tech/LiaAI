package com.Lia.assistant.voice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolCallDeduplicatorTest {

    @Test
    fun testFirstCallIsNotDuplicate() {
        val deduplicator = ToolCallDeduplicator()
        assertFalse(deduplicator.isDuplicate("call_1"))
    }

    @Test
    fun testSecondCallWithSameIdIsDuplicate() {
        val deduplicator = ToolCallDeduplicator()
        assertFalse(deduplicator.isDuplicate("call_1"))
        assertTrue(deduplicator.isDuplicate("call_1"))
    }

    @Test
    fun testBoundedCapacityEvictsOldest() {
        val capacity = 5
        val deduplicator = ToolCallDeduplicator(maxSize = capacity)

        for (i in 1..capacity) {
            assertFalse(deduplicator.isDuplicate("call_$i"))
        }

        // Now add 6th element; call_1 should be evicted
        assertFalse(deduplicator.isDuplicate("call_6"))

        // call_1 should now not be recognized as duplicate because it was evicted
        assertFalse(deduplicator.isDuplicate("call_1"))
    }

    @Test
    fun testResetClearsSeen() {
        val deduplicator = ToolCallDeduplicator()
        assertFalse(deduplicator.isDuplicate("call_1"))
        assertTrue(deduplicator.isDuplicate("call_1"))

        deduplicator.reset()
        assertFalse(deduplicator.isDuplicate("call_1"))
    }
}
