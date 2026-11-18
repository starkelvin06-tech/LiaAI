package com.Lia.assistant.voice

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiLiveClient(private val listener: Listener) {

    interface Listener {
        fun onSetupComplete()
        fun onAudioChunk(bytes: ByteArray)
        fun onInputTranscript(text: String)
        fun onOutputTranscript(text: String)
        fun onToolCall(id: String, name: String, args: JSONObject)
        fun onTurnComplete()
        fun onInterrupted()
        fun onError(message: String)
        fun onClosed()
    }

    companion object {
        const val MODEL = "models/gemini-2.0-flash-live-preview"
        private const val WS_BASE_URL =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    private var webSocket: WebSocket? = null
    private val deduplicator = ToolCallDeduplicator()

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    fun connect(apiKey: String, systemInstruction: String, voiceName: String = "Aoede") {
        disconnect()

        val url = "$WS_BASE_URL?key=$apiKey"
        val request = Request.Builder().url(url).build()

        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                sendSetup(ws, systemInstruction, voiceName)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                parseServerMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                parseServerMessage(bytes.utf8())
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                listener.onError(t.localizedMessage ?: "WebSocket error")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                listener.onClosed()
            }
        })
    }

    private fun sendSetup(ws: WebSocket, systemInstruction: String, voiceName: String) {
        try {
            val setupObj = JSONObject()
            setupObj.put("model", MODEL)

            // Audio generation config
            val genConfig = JSONObject()
            genConfig.put("responseModalities", JSONArray().put("AUDIO"))

            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            val prebuiltVoiceConfig = JSONObject()
            prebuiltVoiceConfig.put("voiceName", voiceName)
            voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
            speechConfig.put("voiceConfig", voiceConfig)
            genConfig.put("speechConfig", speechConfig)

            setupObj.put("generationConfig", genConfig)

            // System instruction
            val sysInstObj = JSONObject()
            val partsArr = JSONArray().put(JSONObject().put("text", systemInstruction))
            sysInstObj.put("parts", partsArr)
            setupObj.put("systemInstruction", sysInstObj)

            // Transcription configs
            setupObj.put("inputAudioTranscription", JSONObject())
            setupObj.put("outputAudioTranscription", JSONObject())

            // Tool function declarations from LiaToolCatalog
            val toolsArr = JSONArray()
            val toolObj = JSONObject()
            val fnDecls = JSONArray()
            for (decl in LiaToolCatalog.declarations()) {
                fnDecls.put(decl)
            }
            toolObj.put("functionDeclarations", fnDecls)
            toolsArr.put(toolObj)
            setupObj.put("tools", toolsArr)

            val rootMsg = JSONObject().put("setup", setupObj)
            ws.send(rootMsg.toString())
        } catch (e: Exception) {
            listener.onError("Setup serialization error: ${e.message}")
        }
    }

    fun sendAudio(pcmBytes: ByteArray) {
        val ws = webSocket ?: return
        if (pcmBytes.isEmpty()) return

        try {
            val b64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
            val audioObj = JSONObject()
                .put("mimeType", "audio/pcm;rate=16000")
                .put("data", b64)

            val realtimeInput = JSONObject().put("audio", audioObj)
            val root = JSONObject().put("realtimeInput", realtimeInput)

            ws.send(root.toString())
        } catch (_: Exception) {
            // Audio streaming exceptions must not crash
        }
    }

    fun sendToolResponse(id: String, name: String, resultJson: JSONObject) {
        val ws = webSocket ?: return
        try {
            val fnResponse = JSONObject()
                .put("id", id)
                .put("name", name)
                .put("response", resultJson)

            val responsesArr = JSONArray().put(fnResponse)
            val toolResponse = JSONObject().put("functionResponses", responsesArr)
            val root = JSONObject().put("toolResponse", toolResponse)

            ws.send(root.toString())
        } catch (e: Exception) {
            listener.onError("Tool response error: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {
        }
        webSocket = null
        deduplicator.reset()
    }

    private fun parseServerMessage(rawText: String) {
        try {
            val root = JSONObject(rawText)

            // Check setup complete
            if (root.has("setupComplete")) {
                listener.onSetupComplete()
                return
            }

            // Silently ignore session updates / goAway
            if (root.has("sessionResumptionUpdate") || root.has("goAway")) {
                return
            }

            // Check tool calls
            if (root.has("toolCall")) {
                val toolCall = root.getJSONObject("toolCall")
                if (toolCall.has("functionCalls")) {
                    val fnCalls = toolCall.getJSONArray("functionCalls")
                    for (i in 0 until fnCalls.length()) {
                        val call = fnCalls.getJSONObject(i)
                        val id = call.optString("id", "")
                        val name = call.optString("name", "")
                        val args = call.optJSONObject("args") ?: JSONObject()

                        if (!deduplicator.isDuplicate(id)) {
                            listener.onToolCall(id, name, args)
                        }
                    }
                }
            }

            // Check serverContent (audio chunks, transcriptions, turn flags)
            if (root.has("serverContent")) {
                val serverContent = root.getJSONObject("serverContent")

                // Model audio turn
                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    if (modelTurn.has("parts")) {
                        val parts = modelTurn.getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val mimeType = inlineData.optString("mimeType", "")
                                if (mimeType.startsWith("audio/pcm")) {
                                    val b64 = inlineData.optString("data", "")
                                    if (b64.isNotEmpty()) {
                                        val pcmBytes = Base64.decode(b64, Base64.DEFAULT)
                                        listener.onAudioChunk(pcmBytes)
                                    }
                                }
                            }
                        }
                    }
                }

                // Input audio transcription (user speech)
                if (serverContent.has("inputTranscription")) {
                    val inputTrans = serverContent.getJSONObject("inputTranscription")
                    val text = inputTrans.optString("text", "").trim()
                    if (text.isNotEmpty()) {
                        listener.onInputTranscript(text)
                    }
                }

                // Output audio transcription (assistant speech)
                if (serverContent.has("outputTranscription")) {
                    val outputTrans = serverContent.getJSONObject("outputTranscription")
                    val text = outputTrans.optString("text", "").trim()
                    if (text.isNotEmpty()) {
                        listener.onOutputTranscript(text)
                    }
                }

                // Turn completed
                if (serverContent.optBoolean("turnComplete", false)) {
                    listener.onTurnComplete()
                }

                // Interrupted
                if (serverContent.optBoolean("interrupted", false)) {
                    listener.onInterrupted()
                }
            }
        } catch (_: Exception) {
            // Never log raw server messages or sensitive user payloads
        }
    }
}
