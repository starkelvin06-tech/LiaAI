package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.Lia.assistant.accessibility.NovaAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Direct flavor action executor.
 * Supports app launching, calling, auto-message sending, and full accessibility screen manipulation.
 */
object ActionExecutor {

    suspend fun execute(context: Context, toolName: String, args: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            when (toolName) {
                "open_app" -> {
                    val appName = args.optString("app_name", "").trim()
                    val pkg = AppLauncher.findApp(context, appName)
                    if (pkg != null && AppLauncher.launch(context, pkg)) {
                        JSONObject().put("status", "success").put("result", "opened_$appName")
                    } else {
                        JSONObject().put("status", "error").put("result", "app_not_found")
                    }
                }

                "call_contact" -> {
                    val contactQuery = args.optString("contact", "").trim()
                    when (val lookup = ContactResolver.lookup(context, contactQuery)) {
                        is ContactResolver.Result.Found -> {
                            val cleanNumber = lookup.phoneNumber
                            val hasCallPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CALL_PHONE
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCallPerm) {
                                val callIntent = Intent(Intent.ACTION_CALL).apply {
                                    data = Uri.parse("tel:$cleanNumber")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(callIntent)
                                JSONObject().put("status", "success").put("result", "calling")
                            } else {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$cleanNumber")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(dialIntent)
                                JSONObject().put("status", "success").put("result", "dialer_opened_no_permission")
                            }
                        }

                        is ContactResolver.Result.NoPermission -> {
                            JSONObject().put("status", "error").put("result", "permission_denied")
                        }

                        is ContactResolver.Result.NotFound -> {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$contactQuery")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(dialIntent)
                            JSONObject().put("status", "error").put("result", "contact_not_found_dialer_opened")
                        }
                    }
                }

                "message_contact" -> {
                    val contactQuery = args.optString("contact", "").trim()
                    val messageText = args.optString("message", "").trim()
                    val platform = args.optString("platform", "sms").lowercase()

                    when (val lookup = ContactResolver.lookup(context, contactQuery)) {
                        is ContactResolver.Result.Found -> {
                            val intent = if (platform == "whatsapp" && CommIntents.isWhatsAppInstalled(context)) {
                                CommIntents.whatsappComposer(context, lookup.phoneNumber, messageText)
                            } else {
                                CommIntents.smsComposer(lookup.phoneNumber, messageText)
                            }
                            context.startActivity(intent)

                            // Direct flavor auto-send: attempt up to 12 retries, waiting 400ms each
                            val service = NovaAccessibilityService.getInstance()
                            if (service != null && NovaAccessibilityService.isEnabled(context)) {
                                var autoSent = false
                                for (attempt in 1..12) {
                                    delay(400)
                                    // WhatsApp web/intent page: tap "Continue to chat" if present
                                    if (platform == "whatsapp") {
                                        service.tapText("Continue to chat")
                                    }

                                    // Tap Send button
                                    val sendResult = service.tapText("Send")
                                    if (sendResult == "tapped") {
                                        autoSent = true
                                        break
                                    }
                                }

                                if (autoSent) {
                                    JSONObject().put("status", "success").put("result", "sent")
                                } else {
                                    JSONObject().put("status", "success").put("result", "composer_opened")
                                }
                            } else {
                                JSONObject().put("status", "success").put("result", "composer_opened")
                            }
                        }

                        is ContactResolver.Result.NoPermission -> {
                            JSONObject().put("status", "error").put("result", "permission_denied")
                        }

                        is ContactResolver.Result.NotFound -> {
                            JSONObject().put("status", "error").put("result", "contact_not_found")
                        }
                    }
                }

                "read_screen" -> {
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val text = service.readScreen()
                        JSONObject().put("status", "success").put("result", text)
                    }
                }

                "tap_text" -> {
                    val targetText = args.optString("text", "").trim()
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val res = service.tapText(targetText)
                        val status = if (res == "tapped") "success" else "error"
                        JSONObject().put("status", status).put("result", res)
                    }
                }

                "type_text" -> {
                    val textToType = args.optString("text", "")
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val res = service.typeText(textToType)
                        val status = if (res == "typed") "success" else "error"
                        JSONObject().put("status", status).put("result", res)
                    }
                }

                "scroll_screen" -> {
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val res = service.scrollScreen()
                        val status = if (res == "scrolled") "success" else "error"
                        JSONObject().put("status", status).put("result", res)
                    }
                }

                "go_back" -> {
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val done = service.performBack()
                        JSONObject().put("status", if (done) "success" else "error").put("result", if (done) "done" else "failed")
                    }
                }

                "go_home" -> {
                    val service = NovaAccessibilityService.getInstance()
                    if (service == null || !NovaAccessibilityService.isEnabled(context)) {
                        JSONObject().put("status", "error").put("result", "accessibility_not_enabled")
                    } else {
                        val done = service.performHome()
                        JSONObject().put("status", if (done) "success" else "error").put("result", if (done) "done" else "failed")
                    }
                }

                else -> {
                    JSONObject().put("status", "error").put("result", "unknown_tool")
                }
            }
        }
}
