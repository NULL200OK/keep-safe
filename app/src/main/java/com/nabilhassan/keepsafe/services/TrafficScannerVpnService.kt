package com.nabilhassan.keepsafe.services

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log

class TrafficScannerVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var running = false

    companion object {
        const val ACTION_START = "START_VPN"
        const val ACTION_STOP = "STOP_VPN"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (running) return
        try {
            val b = Builder()
                .setSession("Keep Safe")
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("10.0.0.1")
                .setBlocking(true)
            try { b.addDisallowedApplication(packageName) } catch (_: Exception) { }
            vpnInterface = b.establish()
            running = true
            Log.i("VPN", "Started")
        } catch (e: Exception) {
            Log.e("VPN", "Failed: ${e.message}")
        }
    }

    private fun stopVpn() {
        running = false
        try { vpnInterface?.close(); vpnInterface = null } catch (_: Exception) { }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}