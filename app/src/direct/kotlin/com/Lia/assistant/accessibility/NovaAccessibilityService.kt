package com.Lia.assistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class NovaAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        private var instance: NovaAccessibilityService? = null

        fun getInstance(): NovaAccessibilityService? = instance

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val expectedComponentName = "${context.packageName}/${NovaAccessibilityService::class.java.name}"
            return enabledServices.split(':').any { it.trim().equals(expectedComponentName, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Events are processed on demand via rootInActiveWindow
    }

    override fun onInterrupt() {
        // No interrupt action required
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun readScreen(): String {
        val root = rootInActiveWindow ?: return "accessibility_not_enabled"
        val sb = StringBuilder()

        fun traverse(node: AccessibilityNodeInfo?) {
            if (node == null) return
            val text = node.text?.toString()?.trim()
            if (!text.isNullOrBlank()) {
                sb.append(text).append("\n")
            }
            val contentDesc = node.contentDescription?.toString()?.trim()
            if (!contentDesc.isNullOrBlank() && contentDesc != text) {
                sb.append("[").append(contentDesc).append("]\n")
            }

            for (i in 0 until node.childCount) {
                traverse(node.getChild(i))
            }
        }

        traverse(root)
        val result = sb.toString().trim()
        return if (result.isEmpty()) "empty_screen" else result.take(4000)
    }

    fun tapText(text: String): String {
        val root = rootInActiveWindow ?: return "accessibility_not_enabled"
        val clean = text.trim()
        val nodes = root.findAccessibilityNodeInfosByText(clean)

        if (nodes.isNullOrEmpty()) {
            return "element_not_found"
        }

        for (node in nodes) {
            if (node.isClickable) {
                val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (clicked) return "tapped"
            }
            // Check clickable parent
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) return "tapped"
                }
                parent = parent.parent
            }
        }

        // Fallback: try clicking the first node anyway
        val fallbackClicked = nodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return if (fallbackClicked) "tapped" else "element_not_clickable"
    }

    fun typeText(text: String): String {
        val root = rootInActiveWindow ?: return "accessibility_not_enabled"
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return "no_focused_field"

        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val set = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        return if (set) "typed" else "typing_failed"
    }

    fun scrollScreen(): String {
        val root = rootInActiveWindow ?: return "accessibility_not_enabled"

        fun findScrollable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.isScrollable) return node
            for (i in 0 until node.childCount) {
                val scrollable = findScrollable(node.getChild(i))
                if (scrollable != null) return scrollable
            }
            return null
        }

        val scrollable = findScrollable(root) ?: return "nothing_scrollable"
        val scrolled = scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        return if (scrolled) "scrolled" else "scroll_failed"
    }

    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
}
