package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * In-memory process-wide cache mapping lowercase app labels to package names.
 * Has a 15-minute TTL.
 */
object InstalledAppLabelCache {
    private const val TTL_MS = 15 * 60 * 1000L // 15 minutes

    private val mutex = Mutex()
    private var cache: Map<String, String> = emptyMap()
    private var lastUpdated: Long = 0L

    suspend fun prewarm(context: Context) {
        withContext(Dispatchers.IO) {
            get(context)
        }
    }

    suspend fun get(context: Context): Map<String, String> {
        val now = System.currentTimeMillis()
        if (cache.isNotEmpty() && (now - lastUpdated < TTL_MS)) {
            return cache
        }

        return mutex.withLock {
            if (cache.isNotEmpty() && (now - lastUpdated < TTL_MS)) {
                return@withLock cache
            }
            val freshMap = buildCache(context)
            cache = freshMap
            lastUpdated = System.currentTimeMillis()
            freshMap
        }
    }

    private fun buildCache(context: Context): Map<String, String> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val map = HashMap<String, String>(resolveInfos.size)

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val label = info.loadLabel(pm).toString().trim().lowercase()
            if (label.isNotEmpty()) {
                map[label] = pkg
            }
        }
        return map
    }
}
