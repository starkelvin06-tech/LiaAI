package com.Lia.assistant.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguagePreferenceTest {

    @Test
    fun testLanguagePreferenceValues() {
        val values = LanguagePreference.entries
        assertEquals(4, values.size)
        assertTrue(values.contains(LanguagePreference.AUTO))
        assertTrue(values.contains(LanguagePreference.HINDI))
        assertTrue(values.contains(LanguagePreference.HINGLISH))
        assertTrue(values.contains(LanguagePreference.ENGLISH))
    }

    @Test
    fun testAutoDescriptionMentionsSpeak() {
        assertTrue(LanguagePreference.AUTO.description.contains("speak", ignoreCase = true))
    }

    @Test
    fun testDisplayNames() {
        assertEquals("Hindi", LanguagePreference.HINDI.displayName)
        assertEquals("Hinglish", LanguagePreference.HINGLISH.displayName)
        assertEquals("English", LanguagePreference.ENGLISH.displayName)
        assertEquals("Auto", LanguagePreference.AUTO.displayName)
    }
}
