package com.Lia.assistant.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.Lia.assistant.voice.VoiceSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.assistantDataStore: DataStore<Preferences> by preferencesDataStore(name = "assistant_settings")

/**
 * DataStore-backed repository with NovaPreferences fallback.
 * Automatically notifies VoiceSessionManager to reload system prompts when updated.
 */
object PersonalityRepository {
    private val KEY_PERSONALITY = stringPreferencesKey("personality")
    private val KEY_LANGUAGE = stringPreferencesKey("language")
    private val KEY_ASSISTANT_NAME = stringPreferencesKey("assistant_name")
    private val KEY_VOICE_NAME = stringPreferencesKey("voice_name")

    fun personalityFlow(context: Context): Flow<Personality> {
        return context.assistantDataStore.data.map { prefs ->
            val name = prefs[KEY_PERSONALITY] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.PERSONALITY,
                Personality.DEFAULT.name
            )
            try {
                Personality.valueOf(name)
            } catch (e: Exception) {
                Personality.DEFAULT
            }
        }
    }

    fun languageFlow(context: Context): Flow<LanguagePreference> {
        return context.assistantDataStore.data.map { prefs ->
            val name = prefs[KEY_LANGUAGE] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.LANGUAGE,
                LanguagePreference.AUTO.name
            )
            try {
                LanguagePreference.valueOf(name)
            } catch (e: Exception) {
                LanguagePreference.AUTO
            }
        }
    }

    fun assistantNameFlow(context: Context): Flow<String> {
        return context.assistantDataStore.data.map { prefs ->
            prefs[KEY_ASSISTANT_NAME] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.ASSISTANT_NAME,
                AssistantBrand.NAME
            ).ifBlank { AssistantBrand.NAME }
        }
    }

    fun voiceNameFlow(context: Context): Flow<String> {
        return context.assistantDataStore.data.map { prefs ->
            prefs[KEY_VOICE_NAME] ?: NovaPreferences.getString(
                context,
                NovaPreferences.Keys.VOICE_NAME,
                "Aoede"
            ).ifBlank { "Aoede" }
        }
    }

    suspend fun getPersonality(context: Context): Personality {
        return try {
            personalityFlow(context).first()
        } catch (e: Exception) {
            val name = NovaPreferences.getString(context, NovaPreferences.Keys.PERSONALITY, Personality.DEFAULT.name)
            try { Personality.valueOf(name) } catch (e: Exception) { Personality.DEFAULT }
        }
    }

    suspend fun getLanguage(context: Context): LanguagePreference {
        return try {
            languageFlow(context).first()
        } catch (e: Exception) {
            val name = NovaPreferences.getString(context, NovaPreferences.Keys.LANGUAGE, LanguagePreference.AUTO.name)
            try { LanguagePreference.valueOf(name) } catch (e: Exception) { LanguagePreference.AUTO }
        }
    }

    suspend fun getAssistantName(context: Context): String {
        return try {
            assistantNameFlow(context).first()
        } catch (e: Exception) {
            NovaPreferences.getString(context, NovaPreferences.Keys.ASSISTANT_NAME, AssistantBrand.NAME)
                .ifBlank { AssistantBrand.NAME }
        }
    }

    suspend fun getVoiceName(context: Context): String {
        return try {
            voiceNameFlow(context).first()
        } catch (e: Exception) {
            NovaPreferences.getString(context, NovaPreferences.Keys.VOICE_NAME, "Aoede")
                .ifBlank { "Aoede" }
        }
    }

    suspend fun setPersonality(context: Context, personality: Personality) {
        NovaPreferences.putString(context, NovaPreferences.Keys.PERSONALITY, personality.name)
        context.assistantDataStore.edit { it[KEY_PERSONALITY] = personality.name }
        VoiceSessionManager.refreshInstructions(context)
    }

    suspend fun setLanguage(context: Context, language: LanguagePreference) {
        NovaPreferences.putString(context, NovaPreferences.Keys.LANGUAGE, language.name)
        context.assistantDataStore.edit { it[KEY_LANGUAGE] = language.name }
        VoiceSessionManager.refreshInstructions(context)
    }

    suspend fun setAssistantName(context: Context, name: String) {
        val clean = name.trim().ifBlank { AssistantBrand.NAME }
        NovaPreferences.putString(context, NovaPreferences.Keys.ASSISTANT_NAME, clean)
        context.assistantDataStore.edit { it[KEY_ASSISTANT_NAME] = clean }
        VoiceSessionManager.refreshInstructions(context)
    }

    suspend fun setVoiceName(context: Context, voiceName: String) {
        val clean = voiceName.trim().ifBlank { "Aoede" }
        NovaPreferences.putString(context, NovaPreferences.Keys.VOICE_NAME, clean)
        context.assistantDataStore.edit { it[KEY_VOICE_NAME] = clean }
        VoiceSessionManager.refreshInstructions(context)
    }
}
