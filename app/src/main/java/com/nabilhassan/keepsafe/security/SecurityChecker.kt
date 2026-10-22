package com.nabilhassan.keepsafe.security

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.nabilhassan.keepsafe.services.TrafficScannerVpnService
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CheckItem(
    val title: String,
    val detail: String,
    val ok: Boolean,
    val weight: Int,
    val fixAction: String? = null
)

data class CheckReport(val items: List<CheckItem>) {
    val score: Int get() = items.filter { it.ok }.sumOf { it.weight }
}

object SecurityChecker {

    private val risky = setOf(
        "android.permission.READ_SMS", "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG", "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.READ_CONTACTS"
    )
    private val trustedInstallers = setOf(
        "com.android.vending", "com.sec.android.app.samsungapps",
        "com.huawei.appmarket", "com.amazon.venezia", "com.xiaomi.mipicks"
    )

    fun run(context: Context): CheckReport {
        val items = mutableListOf<CheckItem>()

        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        items += CheckItem(
            "قفل الشاشة", if (km.isDeviceSecure) "مفعّل" else "هاتفك بلا قفل — أي شخص يمكنه فتحه",
            km.isDeviceSecure, 20, Settings.ACTION_SECURITY_SETTINGS)

        val ageDays = patchAgeDays()
        items += CheckItem(
            "تحديثات الأمان",
            when {
                ageDays == null -> "تعذّر معرفة تاريخ آخر تحديث"
                ageDays <= 120 -> "نظامك محدّث (قبل $ageDays يوم)"
                else -> "آخر تحديث أمني قبل $ageDays يوم — حدّث نظامك"
            }, ageDays != null && ageDays <= 120, 15, "android.settings.SYSTEM_UPDATE_SETTINGS")

        items += CheckItem(
            "إصدار أندرويد",
            "Android ${Build.VERSION.RELEASE}" +
                if (Build.VERSION.SDK_INT >= 31) "" else " — قديم وقد يفتقد حمايات مهمة",
            Build.VERSION.SDK_INT >= 31, 10, Settings.ACTION_DEVICE_INFO_SETTINGS)

        val adb = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        items += CheckItem(
            "تصحيح USB", if (adb) "مفعّل — أوقفه إن لم تكن مطوّرًا" else "معطّل",
            !adb, 10, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)

        val acc = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val accOn = !acc.isNullOrBlank()
        items += CheckItem(
            "خدمات إمكانية الوصول",
            if (accOn) "تطبيقات تتحكم بالشاشة — تأكد أنك تعرفها" else "لا يوجد",
            !accOn, 10, Settings.ACTION_ACCESSIBILITY_SETTINGS)

        val apps = scanApps(context)
        items += CheckItem(
            "تطبيقات من خارج المتجر",
            if (apps.sideloaded.isEmpty()) "كل تطبيقاتك من متجر موثوق"
            else "${apps.sideloaded.size} تطبيق: " + apps.sideloaded.take(4).joinToString("، "),
            apps.sideloaded.size <= 2, 10, Settings.ACTION_APPLICATION_SETTINGS)
        items += CheckItem(
            "تطبيقات بأذونات حساسة كثيرة",
            if (apps.greedy.isEmpty()) "لا يوجد"
            else apps.greedy.take(4).joinToString("، "),
            apps.greedy.isEmpty(), 10, Settings.ACTION_APPLICATION_SETTINGS)

        val vpnOn = TrafficScannerVpnService.running.value
        items += CheckItem(
            "حماية أمان (DNS)",
            if (vpnOn) "مفعّلة" else "غير مفعّلة — فعّلها من الصفحة الرئيسية", vpnOn, 10)

        val pdns = Settings.Global.getString(context.contentResolver, "private_dns_mode")
        val pdnsBad = pdns == "hostname"
        items += CheckItem(
            "DNS خاص",
            if (pdnsBad) "مفعّل ويتجاوز حماية أمان" else "لا يتعارض مع الحماية",
            !pdnsBad, 5, "android.settings.WIRELESS_SETTINGS")

        return CheckReport(items)
    }

    private fun patchAgeDays(): Long? = try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(Build.VERSION.SECURITY_PATCH)
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - d!!.time)
    } catch (_: Exception) { null }

    private class Apps(val sideloaded: List<String>, val greedy: List<String>)

    private fun scanApps(context: Context): Apps {
        val pm = context.packageManager
        val side = mutableListOf<String>()
        val greedy = mutableListOf<String>()
        try {
            val pkgs = if (Build.VERSION.SDK_INT >= 33)
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_PERMISSIONS.toLong()))
            else @Suppress("DEPRECATION") pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            for (p in pkgs) {
                val ai = p.applicationInfo ?: continue
                if (ai.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) continue
                if (p.packageName == context.packageName) continue
                val label = pm.getApplicationLabel(ai).toString()
                val installer = try {
                    if (Build.VERSION.SDK_INT >= 30)
                        pm.getInstallSourceInfo(p.packageName).installingPackageName
                    else @Suppress("DEPRECATION") pm.getInstallerPackageName(p.packageName)
                } catch (_: Exception) { null }
                if (installer == null || installer !in trustedInstallers) side += label
                val n = p.requestedPermissions?.count { it in risky } ?: 0
                if (n >= 4) greedy += label
            }
        } catch (_: Exception) { }
        return Apps(side, greedy)
    }

    /** Things that can silently bypass the DNS protection. */
    fun bypassWarnings(context: Context): List<String> {
        val w = mutableListOf<String>()
        if (Settings.Global.getString(context.contentResolver, "private_dns_mode") == "hostname")
            w += "«DNS الخاص» مفعّل في إعدادات الهاتف ويمكنه تجاوز الحماية."
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val vpns = cm.allNetworks.count {
                cm.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            }
            val ours = if (TrafficScannerVpnService.running.value) 1 else 0
            if (vpns > ours) w += "يوجد تطبيق VPN آخر مفعّل وقد يتجاوز الحماية."
        } catch (_: Exception) { }
        return w
    }
}
