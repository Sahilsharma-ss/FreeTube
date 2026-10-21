package com.freetube.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun FreeTubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FreeTubeDarkColors,
        typography  = FreeTubeTypography,
        content     = content
    )
}
