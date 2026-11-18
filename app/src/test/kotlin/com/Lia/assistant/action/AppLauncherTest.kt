package com.Lia.assistant.action

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class AppLauncherTest {

    // Test pure logic candidate selection matching the implementation in AppLauncher
    private fun findMatchingPackage(query: String, map: Map<String, String>): String? {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return null

        // Exact match
        map[cleanQuery]?.let { return it }

        val queryWords = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        val candidates = map.filter { (key, _) ->
            if (key.length < 3) return@filter false
            val keyWords = key.split(Regex("\\s+")).filter { it.isNotBlank() }

            val directPrefix = key.startsWith(cleanQuery) || cleanQuery.startsWith(key)
            val wordPrefixMatch = queryWords.isNotEmpty() && queryWords.all { qw ->
                keyWords.any { kw -> kw.startsWith(qw) }
            }

            directPrefix || wordPrefixMatch
        }

        if (candidates.isEmpty()) return null

        return candidates.minByOrNull { (key, _) ->
            abs(key.length - cleanQuery.length)
        }?.value
    }

    @Test
    fun testExactMatchTakesPrecedence() {
        val appMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "youtube music" to "com.google.android.apps.youtube.music"
        )
        val result = findMatchingPackage("youtube", appMap)
        assertEquals("com.google.android.youtube", result)
    }

    @Test
    fun testPrefixMatching() {
        val appMap = mapOf(
            "spotify music" to "com.spotify.music",
            "whatsapp" to "com.whatsapp"
        )
        val result = findMatchingPackage("spot", appMap)
        assertEquals("com.spotify.music", result)
    }

    @Test
    fun testLooseSubstringRejected() {
        val appMap = mapOf(
            "chrome" to "com.android.chrome"
        )
        // "rom" is a loose substring inside "chrome", but neither word-prefix nor startsWith
        val result = findMatchingPackage("rom", appMap)
        assertNull(result)
    }
}
