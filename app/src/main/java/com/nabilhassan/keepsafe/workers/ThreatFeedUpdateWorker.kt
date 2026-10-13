package com.nabilhassan.keepsafe.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nabilhassan.keepsafe.MaliciousUrl
import com.nabilhassan.keepsafe.ThreatDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class ThreatFeedUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = ThreatDatabase.getInstance(applicationContext)
            val feeds = listOf(
                "https://openphish.com/feed.txt",
                "https://urlhaus.abuse.ch/downloads/text/"
            )
            for (feed in feeds) {
                try {
                    val text = URL(feed).readText()
                    val urls = text.lines()
                        .filter { it.isNotBlank() && !it.startsWith("#") }
                        .take(500)
                        .map { MaliciousUrl(url = it.trim(), domain = null, source = feed) }
                    db.threatDao().insertAll(urls)
                } catch (_: Exception) { }
            }
            Result.success()
        } catch (e: Exception) { Result.retry() }
    }
}