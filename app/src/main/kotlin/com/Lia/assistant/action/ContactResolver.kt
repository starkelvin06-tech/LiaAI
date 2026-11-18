package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactResolver {

    sealed class Result {
        data class Found(val displayName: String, val phoneNumber: String) : Result()
        data object NoPermission : Result()
        data object NotFound : Result()
    }

    suspend fun lookup(context: Context, query: String): Result = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext Result.NotFound

        // 1. Direct phone number check
        if (looksLikePhoneNumber(trimmed)) {
            val digitsOnly = trimmed.filter { it.isDigit() || it == '+' }
            return@withContext Result.Found(displayName = trimmed, phoneNumber = digitsOnly)
        }

        // 2. Permission check
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return@withContext Result.NoPermission
        }

        // 3. Query Contacts Provider
        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$trimmed%")
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT 5"

        val matches = mutableListOf<Pair<String, String>>()

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.let {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext() && matches.size < 5) {
                    val name = if (nameIdx >= 0) it.getString(nameIdx) else ""
                    val number = if (numberIdx >= 0) it.getString(numberIdx) else ""
                    if (name.isNotBlank() && number.isNotBlank()) {
                        matches.add(name to number)
                    }
                }
            }
        } catch (_: Exception) {
            return@withContext Result.NotFound
        } finally {
            cursor?.close()
        }

        if (matches.isEmpty()) {
            return@withContext Result.NotFound
        }

        // 4. Rank: exact > prefix > contains
        val lowerQuery = trimmed.lowercase()

        val exactMatch = matches.firstOrNull { it.first.trim().equals(lowerQuery, ignoreCase = true) }
        if (exactMatch != null) {
            return@withContext Result.Found(exactMatch.first, exactMatch.second)
        }

        val prefixMatch = matches.firstOrNull { it.first.trim().lowercase().startsWith(lowerQuery) }
        if (prefixMatch != null) {
            return@withContext Result.Found(prefixMatch.first, prefixMatch.second)
        }

        val containsMatch = matches.first()
        return@withContext Result.Found(containsMatch.first, containsMatch.second)
    }

    private fun looksLikePhoneNumber(text: String): Boolean {
        var digitCount = 0
        for (ch in text) {
            when {
                ch.isDigit() -> digitCount++
                ch in " +-()." -> {}
                else -> return false
            }
        }
        return digitCount >= 5
    }
}
