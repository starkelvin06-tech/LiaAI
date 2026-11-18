package com.Lia.assistant.ui.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaPreferences
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ConversationItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long
)

/**
 * Top-level application state remembered at the navigation root.
 * Backed by Compose mutable states and persisted to NovaPreferences and PersonalityRepository.
 */
class NovaAppState(private val context: Context) {
    var themeMode by mutableStateOf("DARK")
    var reducedMotion by mutableStateOf(false)
    var orbStyle by mutableStateOf("DEFAULT")
    var personality by mutableStateOf(Personality.DEFAULT)
    var assistantName by mutableStateOf(AssistantBrand.NAME)
    var userName by mutableStateOf("")
    var language by mutableStateOf(LanguagePreference.AUTO)
    var voiceName by mutableStateOf("Aoede")
    var speechSpeed by mutableFloatStateOf(1.0f)
    var voiceVolume by mutableFloatStateOf(1.0f)
    var conversations by mutableStateOf<List<ConversationItem>>(emptyList())

    init {
        themeMode = NovaPreferences.getString(context, NovaPreferences.Keys.THEME_MODE, "DARK")
        reducedMotion = NovaPreferences.getBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, false)
        orbStyle = NovaPreferences.getString(context, NovaPreferences.Keys.ORB_STYLE, "DEFAULT")
        personality = try {
            Personality.valueOf(NovaPreferences.getString(context, NovaPreferences.Keys.PERSONALITY, Personality.DEFAULT.name))
        } catch (_: Exception) {
            Personality.DEFAULT
        }
        assistantName = NovaPreferences.getString(context, NovaPreferences.Keys.ASSISTANT_NAME, AssistantBrand.NAME).ifBlank { AssistantBrand.NAME }
        userName = NovaPreferences.getString(context, NovaPreferences.Keys.USER_NAME, "")
        language = try {
            LanguagePreference.valueOf(NovaPreferences.getString(context, NovaPreferences.Keys.LANGUAGE, LanguagePreference.AUTO.name))
        } catch (_: Exception) {
            LanguagePreference.AUTO
        }
        voiceName = NovaPreferences.getString(context, NovaPreferences.Keys.VOICE_NAME, "Aoede").ifBlank { "Aoede" }
        speechSpeed = NovaPreferences.getFloat(context, NovaPreferences.Keys.SPEECH_SPEED, 1.0f)
        voiceVolume = NovaPreferences.getFloat(context, NovaPreferences.Keys.VOICE_VOLUME, 1.0f)
    }

    fun applyThemeMode(mode: String) {
        themeMode = mode
        NovaPreferences.putString(context, NovaPreferences.Keys.THEME_MODE, mode)
    }

    fun applyReducedMotion(value: Boolean) {
        reducedMotion = value
        NovaPreferences.putBoolean(context, NovaPreferences.Keys.REDUCED_MOTION, value)
    }

    fun applyOrbStyle(style: String) {
        orbStyle = style
        NovaPreferences.putString(context, NovaPreferences.Keys.ORB_STYLE, style)
    }

    fun applyPersonality(newPersonality: Personality, scope: CoroutineScope) {
        personality = newPersonality
        scope.launch(Dispatchers.IO) {
            PersonalityRepository.setPersonality(context, newPersonality)
        }
    }

    fun applyAssistantName(name: String, scope: CoroutineScope) {
        val clean = name.trim().ifBlank { AssistantBrand.NAME }
        assistantName = clean
        scope.launch(Dispatchers.IO) {
            PersonalityRepository.setAssistantName(context, clean)
        }
    }

    fun applyUserName(name: String) {
        userName = name.trim()
        NovaPreferences.putString(context, NovaPreferences.Keys.USER_NAME, userName)
    }

    fun applyLanguage(newLanguage: LanguagePreference, scope: CoroutineScope) {
        language = newLanguage
        scope.launch(Dispatchers.IO) {
            PersonalityRepository.setLanguage(context, newLanguage)
        }
    }

    fun applyVoiceName(name: String, scope: CoroutineScope) {
        val clean = name.trim().ifBlank { "Aoede" }
        voiceName = clean
        scope.launch(Dispatchers.IO) {
            PersonalityRepository.setVoiceName(context, clean)
        }
    }

    fun applySpeechSpeed(speed: Float) {
        speechSpeed = speed.coerceIn(0.5f, 2.0f)
        NovaPreferences.putFloat(context, NovaPreferences.Keys.SPEECH_SPEED, speechSpeed)
    }

    fun applyVoiceVolume(volume: Float) {
        voiceVolume = volume.coerceIn(0.0f, 1.0f)
        NovaPreferences.putFloat(context, NovaPreferences.Keys.VOICE_VOLUME, voiceVolume)
    }

    fun addConversation(item: ConversationItem) {
        conversations = listOf(item) + conversations.filter { it.id != item.id }
    }
}
