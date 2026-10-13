package com.nabilhassan.keepsafe.services

import android.telecom.Call
import android.telecom.CallScreeningService
import com.nabilhassan.keepsafe.CallerFraudChecker
import com.nabilhassan.keepsafe.SmartNotificationManager

class CallScreeningServiceImpl : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return
        if (CallerFraudChecker.isKnownFraud(number)) {
            respondToCall(callDetails, CallResponse.Builder()
                .setDisallowCall(true).setRejectCall(true).build())
            SmartNotificationManager.notifyRisk(
                applicationContext, number, "رقم احتيال", "Reported fraud", 95)
            return
        }
        val score = CallerFraudChecker.calculateRisk(number)
        if (score >= 50) {
            SmartNotificationManager.notifyRisk(
                applicationContext, number, "رقم مشبوه", "Suspicious", score)
        }
        respondToCall(callDetails, CallResponse.Builder().build())
    }
}