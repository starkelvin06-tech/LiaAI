package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Play Store flavor action executor.
 * Safely opens apps, calls contacts, and opens message drafts without background screen control.
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
                            JSONObject().put("status", "success").put("result", "composer_opened")
                        }

                        is ContactResolver.Result.NoPermission -> {
                            JSONObject().put("status", "error").put("result", "permission_denied")
                        }

                        is ContactResolver.Result.NotFound -> {
                            JSONObject().put("status", "error").put("result", "contact_not_found")
                        }
                    }
                }

                else -> {
                    JSONObject().put("status", "error").put("result", "tool_unavailable_in_play_build")
                }
            }
        }
}
