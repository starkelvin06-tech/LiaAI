package com.Lia.assistant.voice

import org.json.JSONArray
import org.json.JSONObject

/**
 * Tool definitions exposed to Gemini Live in the Google Play flavor.
 * Strictly limited to non-accessibility phone actions.
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
                description = "Open the SMS or WhatsApp message composer for a contact or number.",
                params = mapOf(
                    "contact" to "Recipient contact name or phone number",
                    "message" to "Draft message body",
                    "platform" to "Messaging platform: 'sms' or 'whatsapp' (default is 'sms')"
                ),
                required = listOf("contact", "message")
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
