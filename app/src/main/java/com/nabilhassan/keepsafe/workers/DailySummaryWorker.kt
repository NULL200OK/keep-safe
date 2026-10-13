package com.nabilhassan.keepsafe.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nabilhassan.keepsafe.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val nm = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val b = NotificationCompat.Builder(applicationContext, "fraud_channel")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("📊 أمان | Keep Safe")
                .setContentText("فحص دوري يومي — NULL200OK")
                .setPriority(NotificationCompat.PRIORITY_LOW)
            nm.notify(9999, b.build())
            Result.success()
        } catch (e: Exception) { Result.retry() }
    }
}