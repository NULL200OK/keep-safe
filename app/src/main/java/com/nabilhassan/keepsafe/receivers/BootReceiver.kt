package com.nabilhassan.keepsafe.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.nabilhassan.keepsafe.family.FamilySettings
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService

/** Restarts the protection after the phone boots, if the user enabled auto-start. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        FamilySettings.init(context)
        if (!FamilySettings.autoStart) return
        // Only possible once the user has already granted the VPN consent.
        if (VpnService.prepare(context) != null) return
        try {
            context.startService(Intent(context, TrafficScannerVpnService::class.java)
                .setAction(TrafficScannerVpnService.ACTION_START))
        } catch (_: Exception) { }
    }
}
