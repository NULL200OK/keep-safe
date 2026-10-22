package com.nabilhassan.keepsafe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nabilhassan.keepsafe.settings.AppTheme

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF001219),
    secondary = Color(0xFF48CAE4),
    onSecondary = Color(0xFF001219),
    tertiary = SafeGreen,
    onTertiary = Color(0xFF00201A),
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF252B4A),
    onSurfaceVariant = Color(0xFFB8C0DC),
    outline = Color(0xFF5A6285),
    error = DangerRed,
    onError = Color.White,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0077B6),
    onPrimary = Color.White,
    secondary = Color(0xFF00698F),
    onSecondary = Color.White,
    tertiary = Color(0xFF058A68),
    onTertiary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF0A0E27),
    surface = Color.White,
    onSurface = Color(0xFF0A0E27),
    surfaceVariant = Color(0xFFE3E9F4),
    onSurfaceVariant = Color(0xFF3F4766),
    outline = Color(0xFF7B84A3),
    error = Color(0xFFD62F57),
    onError = Color.White,
)

@Composable
fun KeepSafeTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
