package com.nabilhassan.keepsafe.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nabilhassan.keepsafe.R
import com.nabilhassan.keepsafe.family.BlockLog
import com.nabilhassan.keepsafe.family.FamilySettings

/** Posts a short weekly summary of what the protection blocked. */
class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        FamilySettings.init(applicationContext)
        BlockLog.init(applicationContext)
        if (!FamilySettings.weeklyReport) return Result.success()
        return try {
            val counts = BlockLog.counts(7)
            val total = counts.values.sum()
            val text = if (total == 0) "لم يُحجب أي موقع هذا الأسبوع 👍"
            else "تم حجب $total محاولة: " +
                counts.entries.joinToString("، ") { "${it.key.labelAr} ${it.value}" }
            val b = NotificationCompat.Builder(applicationContext, "fraud_quiet")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("📊 تقرير أمان الأسبوعي")
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager).notify(7003, b.build())
            Result.success()
        } catch (_: Exception) { Result.retry() }
    }
}
