package com.nft.gallery

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surfaceVariant = LightSurfaceVariant,
    inverseOnSurface = LightInverseOnSurface,
    onSurface = LightOnSurface,
    outline = LightOutline,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,

    onPrimaryContainer = Color.Red,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surfaceVariant = DarkSurfaceVariant,
    inverseOnSurface = DarkInverseOnSurface,
    onSurface = DarkOnSurface,
    outline = DarkOutline,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,

    onPrimaryContainer = Color.Red,
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
