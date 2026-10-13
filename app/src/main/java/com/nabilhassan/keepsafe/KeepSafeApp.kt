package com.nabilhassan.keepsafe

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nabilhassan.keepsafe.workers.WorkScheduler

class KeepSafeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createChannels()
        WorkScheduler.scheduleThreatFeedUpdate(this)
        WorkScheduler.scheduleDailySummary(this)
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(
                "fraud_channel", "Fraud Alerts", NotificationManager.IMPORTANCE_HIGH))
            nm.createNotificationChannel(NotificationChannel(
                "fraud_critical", "Critical Alerts", NotificationManager.IMPORTANCE_MAX))
            nm.createNotificationChannel(NotificationChannel(
                "fraud_quiet", "Quiet", NotificationManager.IMPORTANCE_LOW))
        }
    }
}