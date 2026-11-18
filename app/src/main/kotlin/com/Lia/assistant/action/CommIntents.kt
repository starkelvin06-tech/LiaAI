package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.net.Uri

object CommIntents {

    fun smsComposer(phoneNumber: String, messageText: String): Intent {
        val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanNumber")
            putExtra("sms_body", messageText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun whatsappComposer(context: Context, phoneNumber: String, messageText: String): Intent {
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        val encodedText = Uri.encode(messageText)
        val url = "https://wa.me/$digitsOnly?text=$encodedText"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Try direct WhatsApp package targeting
        val pm = context.packageManager
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                pm.getPackageInfo(pkg, 0)
                intent.setPackage(pkg)
                return intent
            } catch (_: Exception) {}
        }

        return intent
    }

    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {}
        }
        return false
    }
}
