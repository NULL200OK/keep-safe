package com.nabilhassan.keepsafe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.nabilhassan.keepsafe.settings.AppTheme

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF001219),
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    onSurface = Color(0xFF0A0E27),
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
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}