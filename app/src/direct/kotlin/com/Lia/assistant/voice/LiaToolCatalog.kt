package com.Lia.assistant.voice

import org.json.JSONArray
import org.json.JSONObject

/**
 * Tool definitions exposed to Gemini Live in the Direct (sideload) flavor.
 * Includes accessibility screen reading/clicking, typing, scrolling, and Website Forge.
 */
object LiaToolCatalog {

    fun declarations(): List<JSONObject> {
        return listOf(
            functionDecl(
                name = "open_app",
                description = "Open an installed Android application by name.",
                params = mapOf("app_name" to "Name of the app to launch (e.g. YouTube, Spotify)"),
                required = listOf("app_name")
            ),
            functionDecl(
                name = "call_contact",
                description = "Place a phone call to a contact by name or to a specific phone number.",
                params = mapOf("contact" to "Contact name or phone number digits"),
                required = listOf("contact")
            ),
            functionDecl(
                name = "message_contact",
                description = "Prepare and auto-send a message via SMS or WhatsApp to a contact.",
                params = mapOf(
                    "contact" to "Recipient contact name or phone number",
                    "message" to "Message body text",
                    "platform" to "Messaging platform: 'sms' or 'whatsapp' (default is 'sms')"
                ),
                required = listOf("contact", "message")
            ),
            functionDecl(
                name = "read_screen",
                description = "Read all visible text elements and descriptions from the current screen via Accessibility.",
                params = emptyMap(),
                required = emptyList()
            ),
            functionDecl(
                name = "tap_text",
                description = "Tap on a button or UI element matching the specified text on the screen.",
                params = mapOf("text" to "Visible text or label of the UI element to click"),
                required = listOf("text")
            ),
            functionDecl(
                name = "type_text",
                description = "Type text into the currently active/focused input field.",
                params = mapOf("text" to "Text to insert into the focused field"),
                required = listOf("text")
            ),
            functionDecl(
                name = "scroll_screen",
                description = "Scroll the current view forward or downward.",
                params = emptyMap(),
                required = emptyList()
            ),
            functionDecl(
                name = "go_back",
                description = "Perform the Android system Back navigation action.",
                params = emptyMap(),
                required = emptyList()
            ),
            functionDecl(
                name = "go_home",
                description = "Perform the Android system Home navigation action.",
                params = emptyMap(),
                required = emptyList()
            ),
            functionDecl(
                name = "build_website",
                description = "Launch the Website Forge 3D build scene to stream and render a custom animated web page.",
                params = mapOf(
                    "prompt" to "Topic or description of the website (e.g. 'cafe website in Mumbai')",
                    "title" to "Website title"
                ),
                required = listOf("prompt")
            )
        )
    }

    private fun functionDecl(
        name: String,
        description: String,
        params: Map<String, String>,
        required: List<String>
    ): JSONObject {
        val propertiesObj = JSONObject()
        for ((paramName, paramDesc) in params) {
            propertiesObj.put(
                paramName,
                JSONObject()
                    .put("type", "STRING")
                    .put("description", paramDesc)
            )
        }

        val requiredArray = JSONArray()
        for (r in required) {
            requiredArray.put(r)
        }

        return JSONObject()
            .put("name", name)
            .put("description", description)
            .put(
                "parameters",
                JSONObject()
                    .put("type", "OBJECT")
                    .put("properties", propertiesObj)
                    .put("required", requiredArray)
            )
    }
}
