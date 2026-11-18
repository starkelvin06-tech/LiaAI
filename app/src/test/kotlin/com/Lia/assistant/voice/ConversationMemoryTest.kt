package com.Lia.assistant.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationMemoryTest {

    @Test
    fun testEmptyMemoryReturnsNullRecap() {
        val memory = ConversationMemory()
        assertEquals(0, memory.size)
        assertNull(memory.recap())
    }

    @Test
    fun testAddTurnsAndRecapFormat() {
        val memory = ConversationMemory()
        memory.addUserTurn("Hello Lia")
        memory.addAssistantTurn("Hi! How can I help you today?")

        assertEquals(2, memory.size)
        val recap = memory.recap()
        assertNotNull(recap)
        assertTrue(recap!!.contains("User: Hello Lia"))
        assertTrue(recap.contains("You: Hi! How can I help you today?"))
    }

    @Test
    fun testMaxTurnsRollingWindow() {
        val memory = ConversationMemory()
        for (i in 1..20) {
            memory.addUserTurn("User message $i")
        }

        assertEquals(12, memory.size)
        val recap = memory.recap()
        assertNotNull(recap)
        assertTrue(recap!!.contains("User message 20"))
        assertTrue(!recap.contains("User message 1\n"))
    }

    @Test
    fun testClear() {
        val memory = ConversationMemory()
        memory.addUserTurn("Test")
        memory.clear()
        assertEquals(0, memory.size)
        assertNull(memory.recap())
    }
}
