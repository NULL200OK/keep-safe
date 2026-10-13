package com.nabilhassan.keepsafe

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.ScanResult

data class WifiNetworkInfo(
    val ssid: String,
    val bssid: String,
    val security: String,
    val riskScore: Int,
    val riskVerdictAr: String,
    val riskVerdictEn: String
)

object WifiSecurityScanner {

    @Suppress("MissingPermission")
    fun scanNetworks(context: Context): List<WifiNetworkInfo> {
        val wm = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val results = try { wm.scanResults } catch (e: Exception) { null } ?: return emptyList()

        val groups = results.groupBy { it.SSID }
        val evilTwins = groups.filter { (_, list) ->
            list.size > 1 && list.map { it.BSSID }.distinct().size > 1
        }.keys

        return results.map { analyzeNetwork(it, it.SSID in evilTwins) }
    }

    private fun analyzeNetwork(r: ScanResult, evil: Boolean): WifiNetworkInfo {
        val security = when {
            r.capabilities.contains("WPA3") -> "WPA3"
            r.capabilities.contains("WPA2") -> "WPA2"
            r.capabilities.contains("WPA") -> "WPA"
            r.capabilities.contains("WEP") -> "WEP"
            else -> "Open"
        }
        var score = 0
        when (security) {
            "Open" -> score += 50
            "WEP" -> score += 40
            "WPA" -> score += 20
        }
        if (evil) score += 60
        if (r.level > -30) score += 10

        val total = score.coerceIn(0, 100)
        val (ar, en) = when {
            total <= 20 -> "آمنة" to "Safe"
            total <= 50 -> "مشبوهة" to "Suspicious"
            total <= 80 -> "خطر مرتفع" to "High Risk"
            else -> "خطر شديد" to "Critical"
        }

        return WifiNetworkInfo(
            ssid = r.SSID.ifBlank { "(مخفي)" },
            bssid = r.BSSID,
            security = security,
            riskScore = total,
            riskVerdictAr = ar,
            riskVerdictEn = en
        )
    }
}