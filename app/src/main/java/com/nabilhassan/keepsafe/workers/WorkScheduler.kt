package com.nabilhassan.keepsafe.workers

import android.content.Context
import androidx.work.*

object WorkScheduler {
    fun scheduleThreatFeedUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val req = PeriodicWorkRequestBuilder<ThreatFeedUpdateWorker>(
            6, java.util.concurrent.TimeUnit.HOURS
        ).setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "threat_feed_update", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }

    fun scheduleWeeklyReport(context: Context) {
        val req = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
            7, java.util.concurrent.TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "weekly_report", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }

    fun scheduleDailySummary(context: Context) {
        val req = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary", ExistingPeriodicWorkPolicy.KEEP, req
        )
    }
}