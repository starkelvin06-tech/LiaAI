package com.Lia.assistant.accessibility

import android.content.Context

/**
 * Play flavor stub.
 * AccessibilityService is entirely removed at compile time for Google Play policy compliance.
 */
class NovaAccessibilityService {

    companion object {
        fun getInstance(): NovaAccessibilityService? = null

        fun isEnabled(context: Context): Boolean = false
    }

    fun readScreen(): String = "accessibility_not_enabled"

    fun tapText(text: String): String = "accessibility_not_enabled"

    fun typeText(text: String): String = "accessibility_not_enabled"

    fun scrollScreen(): String = "accessibility_not_enabled"

    fun performBack(): Boolean = false

    fun performHome(): Boolean = false
}
