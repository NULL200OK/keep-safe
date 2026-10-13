package com.nabilhassan.keepsafe.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.nabilhassan.keepsafe.SmartNotificationManager
import com.nabilhassan.keepsafe.SmsPhishingScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress ?: "Unknown"
        val body = messages.joinToString(" ") { it.messageBody }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val r = SmsPhishingScanner.analyzeSms(sender, body)
                if (r.isPhishing) {
                    SmartNotificationManager.notifyRisk(
                        context, sender, r.verdictAr, r.verdictEn,
                        (r.probability * 100).toInt()
                    )
                }
            } finally { pending.finish() }
        }
    }
}