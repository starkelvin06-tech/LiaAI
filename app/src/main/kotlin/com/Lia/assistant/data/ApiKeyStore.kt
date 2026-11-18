package com.Lia.assistant.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores and retrieves the user-entered Gemini API key.
 * Stored locally in private SharedPreferences.
 * Never hardcoded and never logged.
 */
object ApiKeyStore {
    private const val PREFS_NAME = "nova_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getKey(context: Context): String? {
        val key = getPrefs(context).getString(KEY_GEMINI_API_KEY, null)?.trim()
        return if (key.isNullOrBlank()) null else key
    }

    fun saveKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun hasKey(context: Context): Boolean {
        return !getKey(context).isNullOrBlank()
    }
}
