package com.nabilhassan.keepsafe

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object SmartNotificationManager {
    fun notifyRisk(context: Context, host: String, vAr: String, vEn: String, score: Int) {
        val channel = if (score >= 80) "fraud_critical" else "fraud_channel"
        val b = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("⚠️ تحذير | Warning")
            .setContentText("$host — $vAr / $vEn ($score/100)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), b.build())
    }
}