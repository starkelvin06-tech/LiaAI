package com.Lia.assistant.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import kotlin.math.abs

object AppLauncher {

    suspend fun findApp(context: Context, query: String): String? {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return null

        val appMap = InstalledAppLabelCache.get(context)

        // 1. Exact match first
        appMap[cleanQuery]?.let { return it }

        // 2. Strict prefix / token match candidates (never loose substring)
        val queryWords = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        val candidates = appMap.filter { (key, _) ->
            if (key.length < 3) return@filter false

            val keyWords = key.split(Regex("\\s+")).filter { it.isNotBlank() }

            val directPrefix = key.startsWith(cleanQuery) || cleanQuery.startsWith(key)
            val wordPrefixMatch = queryWords.isNotEmpty() && queryWords.all { qw ->
                keyWords.any { kw -> kw.startsWith(qw) }
            }

            directPrefix || wordPrefixMatch
        }

        if (candidates.isEmpty()) return null

        // Pick the candidate whose label length is closest to query length
        val bestEntry = candidates.minByOrNull { (key, _) ->
            abs(key.length - cleanQuery.length)
        }

        return bestEntry?.value
    }

    fun launch(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }
}
