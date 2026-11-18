package com.Lia.assistant.voice

class ToolCallDeduplicator(private val maxSize: Int = 256) {
    private val seen = LinkedHashSet<String>()
    private val lock = Any()

    fun isDuplicate(callId: String): Boolean {
        if (callId.isBlank()) return false
        synchronized(lock) {
            if (seen.contains(callId)) {
                return true
            }
            if (seen.size >= maxSize) {
                val iterator = seen.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            seen.add(callId)
            return false
        }
    }

    fun reset() {
        synchronized(lock) {
            seen.clear()
        }
    }
}
