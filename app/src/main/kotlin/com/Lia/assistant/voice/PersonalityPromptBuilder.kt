package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object PersonalityPromptBuilder {
    fun build(
        personality: Personality,
        language: LanguagePreference,
        detectedLanguage: LanguageDetector.Result,
        recap: String?,
        assistantName: String
    ): String {
        val sections = mutableListOf<String>()

        // (a) Base identity
        sections.add(
            "You are $assistantName, a persistent personal voice assistant living on the user's phone. " +
            "Keep replies short and conversational, like spoken conversation. Do not write markdown, code blocks, or bullet lists in spoken voice turns."
        )

        // (b) Flavor capabilities
        sections.add(LiaCapabilityPrompts.toolInstruction(assistantName))

        // (c) Personality system instruction
        sections.add(personality.systemInstruction)

        // (d) Language policy
        val languageInstruction = when (language) {
            LanguagePreference.AUTO -> {
                "Language Policy: Match the user's language turn by turn. " +
                "If they speak Hindi, reply in Hindi. If Hinglish, reply in natural Hinglish. If English, reply in English. " +
                "Never translate what the user said back to them; simply respond in that language."
            }
            LanguagePreference.HINDI -> {
                "Language Policy: Always respond in pure, fluent Hindi using Devanagari script."
            }
            LanguagePreference.HINGLISH -> {
                "Language Policy: Always respond in natural, everyday Hinglish (colloquial Hindi written in Latin/English alphabet)."
            }
            LanguagePreference.ENGLISH -> {
                "Language Policy: Always respond in clear, natural English."
            }
        }
        sections.add(languageInstruction)

        // (e) Safety rules
        sections.add(
            "Safety and Etiquette: Never claim to be human or to have a physical body. " +
            "No sexually explicit content. If the user asks to change the topic or tone, respect it immediately without argument. " +
            "Vary your phrasing across responses so you do not repeat boilerplate greetings or sentence structures."
        )

        // (f) Continuing conversation recap
        if (!recap.isNullOrBlank()) {
            sections.add("This is a continuing conversation — do not greet again.\nRecap:\n$recap")
        }

        return sections.joinToString("\n\n")
    }
}
