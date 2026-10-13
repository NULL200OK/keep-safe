package com.nabilhassan.keepsafe.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

enum class AppLanguage(val code: String, val displayAr: String, val displayEn: String) {
    ARABIC("ar", "العربية", "Arabic"),
    ENGLISH("en", "الإنجليزية", "English"),
    FRENCH("fr", "الفرنسية", "French"),
    SPANISH("es", "الإسبانية", "Spanish"),
    TURKISH("tr", "التركية", "Turkish"),
    SYSTEM("system", "النظام", "System")
}

enum class AppTheme(val displayAr: String, val displayEn: String) {
    LIGHT("نهاري", "Light"),
    DARK("ليلي", "Dark"),
    SYSTEM("تلقائي", "Auto")
}

object SettingsManager {

    private const val PREFS = "keep_safe_settings"
    private const val KEY_THEME = "theme"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_NOTIFICATIONS = "notifications"
    private const val KEY_AUTO_UPDATE = "auto_update"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getTheme(context: Context): AppTheme =
        try { AppTheme.valueOf(prefs(context).getString(KEY_THEME, "SYSTEM") ?: "SYSTEM") }
        catch (e: Exception) { AppTheme.SYSTEM }

    fun setTheme(context: Context, theme: AppTheme) {
        prefs(context).edit().putString(KEY_THEME, theme.name).apply()
    }

    fun getLanguage(context: Context): AppLanguage =
        try { AppLanguage.valueOf(prefs(context).getString(KEY_LANGUAGE, "SYSTEM") ?: "SYSTEM") }
        catch (e: Exception) { AppLanguage.SYSTEM }

    fun setLanguage(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    fun isNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATIONS, true)

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    fun isAutoUpdateEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_UPDATE, true)

    fun setAutoUpdateEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_UPDATE, enabled).apply()
    }
}