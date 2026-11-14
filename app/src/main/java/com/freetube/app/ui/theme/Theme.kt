package com.freetube.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ── Fallback dark scheme (AMOLED) used on Android < 12 ──────────────────────
private val FreeTubeDarkColors = darkColorScheme(
    primary            = FTRed,
    onPrimary          = White,
    primaryContainer   = FTRedDim,
    onPrimaryContainer = White,
    secondary          = WhiteDim,
    onSecondary        = Black,
    background         = Black,
    onBackground       = White,
    surface            = BlackSurface,
    onSurface          = White,
    surfaceVariant     = BlackCard,
    onSurfaceVariant   = WhiteDim,
    outline            = DividerColor,
    error              = FTRed,
    onError            = White
)

// ── Fallback light scheme used on Android < 12 ───────────────────────────────
private val FreeTubeLightColors = lightColorScheme(
    primary            = FTRed,
    onPrimary          = White,
    primaryContainer   = FTRedGlow,
    onPrimaryContainer = FTRedDim,
    secondary          = OnLightMuted,
    onSecondary        = White,
    background         = LightBackground,
    onBackground       = OnLight,
    surface            = LightSurface,
    onSurface          = OnLight,
    surfaceVariant     = LightCard,
    onSurfaceVariant   = OnLightMuted,
    outline            = LightDivider,
    error              = FTRed,
    onError            = White
)

@Composable
fun FreeTubeTheme(content: @Composable () -> Unit) {
    val context   = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    // Material You dynamic colors on Android 12+ (follows device wallpaper palette)
    // Falls back to FreeTube red-accent scheme on older devices
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
            dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme ->
            dynamicLightColorScheme(context)
        darkTheme  -> FreeTubeDarkColors
        else       -> FreeTubeLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = FreeTubeTypography,
        content     = content
    )
}
