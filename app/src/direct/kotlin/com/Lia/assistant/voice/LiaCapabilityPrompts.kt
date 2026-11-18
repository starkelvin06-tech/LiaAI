package com.Lia.assistant.voice

/**
 * Direct sideload distribution tool capability instructions.
 * Full accessibility screen reading, control, auto-send, and social UI agents.
 */
object LiaCapabilityPrompts {
    fun toolInstruction(assistantName: String): String {
        return "Capabilities: You can open apps, call contacts, prepare and send messages, read the screen content, " +
                "tap UI elements, type text, scroll screens, go back, and go home. " +
                "You can run visual UI agents on Instagram, Facebook, and WhatsApp. " +
                "CRITICAL SAFETY RULE: Always ask for explicit user confirmation before executing any irreversible action (such as sending a message, publishing a post, or deleting data)."
    }
}
