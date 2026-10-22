package com.nabilhassan.keepsafe.family

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ProtectionLevel(val titleAr: String, val descAr: String, val upstream: String) {
    BASIC("أساسي", "يحجب المواقع الخبيثة والاحتيالية فقط", "1.1.1.2"),
    FAMILY("عائلي", "يحجب الخبيثة + المواقع الإباحية", "1.1.1.3"),
    STRICT("صارم", "عائلي + القمار والمحتوى العنيف", "1.1.1.3")
}

enum class BlockReason(val labelAr: String) {
    PROVIDER("محتوى ضار / للبالغين"),
    CUSTOM("قائمتك المخصصة"),
    STRICT("قمار / عنف"),
    SCHEDULE("وقت الحظر")
}

/** Central store for family-protection preferences. Call [init] once from Application. */
object FamilySettings {
    private lateinit var prefs: SharedPreferences

    private val _beginner = MutableStateFlow(true)
    val beginner: StateFlow<Boolean> = _beginner.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext
            .getSharedPreferences("family_settings", Context.MODE_PRIVATE)
        _beginner.value = prefs.getBoolean("beginner", true)
    }

    var level: ProtectionLevel
        get() = try {
            ProtectionLevel.valueOf(prefs.getString("level", "FAMILY") ?: "FAMILY")
        } catch (_: Exception) { ProtectionLevel.FAMILY }
        set(v) { prefs.edit().putString("level", v.name).apply() }

    var safeSearch: Boolean
        get() = prefs.getBoolean("safe_search", true)
        set(v) { prefs.edit().putBoolean("safe_search", v).apply() }

    var scheduleEnabled: Boolean
        get() = prefs.getBoolean("sched_on", false)
        set(v) { prefs.edit().putBoolean("sched_on", v).apply() }

    var scheduleStart: Int
        get() = prefs.getInt("sched_start", 22)
        set(v) { prefs.edit().putInt("sched_start", v.mod(24)).apply() }

    var scheduleEnd: Int
        get() = prefs.getInt("sched_end", 6)
        set(v) { prefs.edit().putInt("sched_end", v.mod(24)).apply() }

    var autoStart: Boolean
        get() = prefs.getBoolean("auto_start", false)
        set(v) { prefs.edit().putBoolean("auto_start", v).apply() }

    var weeklyReport: Boolean
        get() = prefs.getBoolean("weekly", true)
        set(v) { prefs.edit().putBoolean("weekly", v).apply() }

    var alertOnBlock: Boolean
        get() = prefs.getBoolean("alert_block", true)
        set(v) { prefs.edit().putBoolean("alert_block", v).apply() }

    var beginnerMode: Boolean
        get() = prefs.getBoolean("beginner", true)
        set(v) {
            prefs.edit().putBoolean("beginner", v).apply()
            _beginner.value = v
        }

    var customBlock: Set<String>
        get() = prefs.getStringSet("custom_block", emptySet()) ?: emptySet()
        set(v) { prefs.edit().putStringSet("custom_block", v).apply() }

    var customAllow: Set<String>
        get() = prefs.getStringSet("custom_allow", emptySet()) ?: emptySet()
        set(v) { prefs.edit().putStringSet("custom_allow", v).apply() }

    /** True while the bedtime schedule says everything should be blocked. */
    fun isScheduleBlockedNow(): Boolean {
        if (!scheduleEnabled) return false
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val s = scheduleStart
        val e = scheduleEnd
        return when {
            s == e -> false
            s < e -> h in s until e
            else -> h >= s || h < e
        }
    }

    fun normalizeDomain(raw: String): String? {
        val d = raw.trim().lowercase(Locale.ROOT)
            .removePrefix("http://").removePrefix("https://")
            .substringBefore('/').substringBefore(':').removePrefix("www.")
        return if (d.length >= 3 && d.contains('.') &&
            d.all { it.isLetterOrDigit() || it == '.' || it == '-' }) d else null
    }
}

/** Persistent log of blocked lookups, used for the report and the blocked list. */
object BlockLog {
    private const val PREFS = "block_log"
    private const val MAX = 200
    private lateinit var prefs: SharedPreferences
    private val dayFmt = SimpleDateFormat("yyyyMMdd", Locale.US)

    data class Entry(val time: Long, val reason: BlockReason, val domain: String)

    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    fun init(context: Context) {
        if (!::prefs.isInitialized)
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    @Synchronized
    fun record(domain: String, reason: BlockReason) {
        val now = System.currentTimeMillis()
        val day = dayFmt.format(Date(now))
        val line = "$now|${reason.name}|$domain"
        val old = prefs.getString("recent", "") ?: ""
        val lines = (listOf(line) + old.split("\n").filter { it.isNotBlank() }).take(MAX)
        val key = "c_${day}_${reason.name}"
        prefs.edit()
            .putString("recent", lines.joinToString("\n"))
            .putInt(key, prefs.getInt(key, 0) + 1)
            .apply()
        _version.value++
    }

    @Synchronized
    fun recent(limit: Int = 30): List<Entry> {
        val raw = prefs.getString("recent", "") ?: return emptyList()
        return raw.split("\n").mapNotNull { l ->
            val p = l.split("|", limit = 3)
            if (p.size < 3) null else try {
                Entry(p[0].toLong(), BlockReason.valueOf(p[1]), p[2])
            } catch (_: Exception) { null }
        }.take(limit)
    }

    /** Blocked counts per reason across the last [days] days (today included). */
    @Synchronized
    fun counts(days: Int): Map<BlockReason, Int> {
        val result = mutableMapOf<BlockReason, Int>()
        val cal = Calendar.getInstance()
        repeat(days) {
            val day = dayFmt.format(cal.time)
            BlockReason.entries.forEach { r ->
                val n = prefs.getInt("c_${day}_${r.name}", 0)
                if (n > 0) result[r] = (result[r] ?: 0) + n
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result
    }

    fun todayTotal(): Int = counts(1).values.sum()
    fun weekTotal(): Int = counts(7).values.sum()

    @Synchronized
    fun clear() {
        prefs.edit().clear().apply()
        _version.value++
    }
}
