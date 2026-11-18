package com.Lia.assistant.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectorTest {

    @Test
    fun testBlankOrEmptyReturnsUnknown() {
        assertEquals(LanguageDetector.Result.UNKNOWN, LanguageDetector.detect(""))
        assertEquals(LanguageDetector.Result.UNKNOWN, LanguageDetector.detect("   "))
    }

    @Test
    fun testDevanagariReturnsHindi() {
        assertEquals(LanguageDetector.Result.HINDI, LanguageDetector.detect("नमस्ते"))
        assertEquals(LanguageDetector.Result.HINDI, LanguageDetector.detect("आप कैसे हैं?"))
        assertEquals(LanguageDetector.Result.HINDI, LanguageDetector.detect("आज का मौसम कैसा है"))
    }

    @Test
    fun testHinglishDetection() {
        assertEquals(LanguageDetector.Result.HINGLISH, LanguageDetector.detect("kya haal hai yaar"))
        assertEquals(LanguageDetector.Result.HINGLISH, LanguageDetector.detect("bhai sach mein kya baat hai"))
        assertEquals(LanguageDetector.Result.HINGLISH, LanguageDetector.detect("aaj sham ko milte hain"))
        assertEquals(LanguageDetector.Result.HINGLISH, LanguageDetector.detect("mujhe thoda pani chahiye"))
    }

    @Test
    fun testEnglishDetection() {
        assertEquals(LanguageDetector.Result.ENGLISH, LanguageDetector.detect("what is the weather today"))
        assertEquals(LanguageDetector.Result.ENGLISH, LanguageDetector.detect("hello how are you doing"))
        assertEquals(LanguageDetector.Result.ENGLISH, LanguageDetector.detect("play some jazz music on spotify"))
    }
}
