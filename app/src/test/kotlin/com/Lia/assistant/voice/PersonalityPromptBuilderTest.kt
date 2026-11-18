package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalityPromptBuilderTest {

    @Test
    fun testAllPersonalitiesBuildNonEmptyPrompt() {
        for (personality in Personality.entries) {
            for (language in LanguagePreference.entries) {
                val prompt = PersonalityPromptBuilder.build(
                    personality = personality,
                    language = language,
                    detectedLanguage = LanguageDetector.Result.UNKNOWN,
                    recap = null,
                    assistantName = "Lia"
                )
                assertTrue("Prompt for $personality in $language should not be empty", prompt.isNotBlank())
                assertTrue("Prompt should contain personality instruction", prompt.contains(personality.systemInstruction))
                assertTrue("Prompt should contain safety instruction", prompt.contains("Never claim to be human"))
                assertFalse("Prompt without recap should not contain continuing conversation note", prompt.contains("This is a continuing conversation"))
            }
        }
    }

    @Test
    fun testRecapAppearsOnlyWhenProvided() {
        val promptWithRecap = PersonalityPromptBuilder.build(
            personality = Personality.NORMAL,
            language = LanguagePreference.AUTO,
            detectedLanguage = LanguageDetector.Result.UNKNOWN,
            recap = "User: Book a ride\nYou: Setting destination...",
            assistantName = "Lia"
        )
        assertTrue(promptWithRecap.contains("This is a continuing conversation — do not greet again."))
        assertTrue(promptWithRecap.contains("Book a ride"))
    }

    @Test
    fun testAutoLanguagePolicy() {
        val prompt = PersonalityPromptBuilder.build(
            personality = Personality.FRIENDLY,
            language = LanguagePreference.AUTO,
            detectedLanguage = LanguageDetector.Result.UNKNOWN,
            recap = null,
            assistantName = "Lia"
        )
        assertTrue(prompt.contains("Match the user's language turn by turn"))
    }
}
