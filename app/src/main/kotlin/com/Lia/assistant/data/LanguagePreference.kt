package com.Lia.assistant.data

enum class LanguagePreference(
    val displayName: String,
    val description: String
) {
    AUTO("Auto", "Follows whatever you speak"),
    HINDI("Hindi", "Always respond in Hindi"),
    HINGLISH("Hinglish", "Mix of Hindi and English"),
    ENGLISH("English", "Always respond in English")
}
