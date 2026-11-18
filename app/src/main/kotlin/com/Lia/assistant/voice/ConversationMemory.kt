package com.Lia.assistant.voice

class ConversationMemory {
    enum class Role {
        USER,
        ASSISTANT
    }

    data class Turn(
        val role: Role,
        val text: String
    )

    private val turns = ArrayDeque<Turn>()
    private val lock = Any()

    companion object {
        const val MAX_TURNS = 12
        const val MAX_RECAP_CHARS = 1600
    }

    val size: Int
        get() = synchronized(lock) { turns.size }

    fun addUserTurn(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        synchronized(lock) {
            if (turns.size >= MAX_TURNS) {
                turns.removeFirst()
            }
            turns.addLast(Turn(Role.USER, clean))
        }
    }

    fun addAssistantTurn(text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        synchronized(lock) {
            if (turns.size >= MAX_TURNS) {
                turns.removeFirst()
            }
            turns.addLast(Turn(Role.ASSISTANT, clean))
        }
    }

    fun recap(): String? {
        synchronized(lock) {
            if (turns.isEmpty()) return null
            val sb = StringBuilder()
            for (turn in turns) {
                val prefix = if (turn.role == Role.USER) "User: " else "You: "
                sb.append(prefix).append(turn.text).append("\n")
            }
            val fullRecap = sb.toString().trimEnd()
            return if (fullRecap.length > MAX_RECAP_CHARS) {
                fullRecap.takeLast(MAX_RECAP_CHARS)
            } else {
                fullRecap
            }
        }
    }

    fun clear() {
        synchronized(lock) {
            turns.clear()
        }
    }
}
