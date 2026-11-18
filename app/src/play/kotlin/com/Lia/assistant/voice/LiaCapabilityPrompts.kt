package com.Lia.assistant.voice

/**
 * Play Store compliant tool capability instructions.
 * Google Play policy strictly forbids accessibility screen control.
 */
object LiaCapabilityPrompts {
    fun toolInstruction(assistantName: String): String {
        return "Capabilities: You can open installed apps, initiate phone calls, and open message composers (SMS and WhatsApp) for the user to review. " +
                "You CANNOT read the screen, tap, type, scroll, or control the UI. " +
                "You CANNOT silently post or send messages without the user manually confirming in the composer."
    }
}
