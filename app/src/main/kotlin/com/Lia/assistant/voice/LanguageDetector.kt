package com.Lia.assistant.voice

object LanguageDetector {
    enum class Result {
        HINDI,
        HINGLISH,
        ENGLISH,
        UNKNOWN
    }

    private val HINGLISH_MARKERS = setOf(
        "hai", "hain", "ho", "kya", "kyun", "kaise", "nahi", "nahin", "haan",
        "mujhe", "tumhe", "aap", "mera", "meri", "mere", "yaar", "bhai",
        "accha", "achha", "theek", "matlab", "karna", "karo", "raha", "rahi", "rahe",
        "chahiye", "batao", "suno", "dekho", "abhi", "phir", "lekin", "bahut",
        "thoda", "kuch", "sab", "wala", "wali", "waala", "kar", "bhi", "toh",
        "to", "jo", "jab", "tab", "agar", "par", "pe", "se", "mein", "ka",
        "ki", "ke", "ne", "ko", "hoga", "hogi", "honge", "gaya", "gayi", "gaye",
        "aaya", "aayi", "aaye", "liya", "diya", "jao", "aao", "ek", "do", "teen",
        "chaar", "paanch", "tha", "thi", "the", "hoti", "hota", "hote", "sirf",
        "woh", "yeh", "ye", "wo", "unka", "unki", "unhe", "usse", "uska", "uski",
        "apna", "apni", "apne"
    )

    fun detect(text: String): Result {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return Result.UNKNOWN

        // 1. Check for Devanagari script (U+0900..U+097F)
        for (ch in trimmed) {
            if (ch in '\u0900'..'\u097F') {
                return Result.HINDI
            }
        }

        // 2. Tokenize words (ignoring punctuation)
        val words = trimmed.lowercase()
            .split(Regex("[^a-zA-Z']+"))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return Result.UNKNOWN

        var hits = 0
        for (w in words) {
            if (w in HINGLISH_MARKERS) {
                hits++
            }
        }

        return if (words.size <= 4) {
            if (hits >= 1) Result.HINGLISH else Result.ENGLISH
        } else {
            val ratio = hits.toFloat() / words.size.toFloat()
            if (hits >= 2 || ratio >= 0.15f) Result.HINGLISH else Result.ENGLISH
        }
    }
}
