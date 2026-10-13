package com.nabilhassan.keepsafe.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun wrap(context: Context, languageCode: String): Context {
        if (languageCode == "system") return context
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val list = LocaleList(locale)
            LocaleList.setDefault(list)
            config.setLocales(list)
            return context.createConfigurationContext(config)
        } else {
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    fun restartApp(activity: Activity) {
        val intent = activity.packageManager
            .getLaunchIntentForPackage(activity.packageName) ?: return
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        activity.startActivity(intent)
        activity.finishAffinity()
        Runtime.getRuntime().exit(0)
    }
}