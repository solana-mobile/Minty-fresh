package com.solanamobile.mintyfresh.composable.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightOnSurface = Color(0xFF000000)
val LightOnSurfaceVariant = Color(0xFFC7C7C7)
val LightOutline = Color(0xFF79747E)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF8F8F8)
val LightSurfaceVariant = Color(0xFFF2F2F7)

val LightPrimary = Color(0xFF000000)
val LightInverseOnSurface = Color(0xFFF5EFF4)
val LightTertiary = Color(0xFF7F21E5)
val LightOnTertiary = Color(0xFFFFFBFF)

val DarkPrimary = Color(0xFFD8B9FF)
val DarkBackground = Color(0xFF000000)
val DarkSurfaceVariant = Color(0xFF1E1A22)
val DarkInverseOnSurface = Color(0xFF322F33)
val DarkOnSurface = Color(0xFFE7E1E5)
val DarkOutline = Color(0xFF958E99)
val DarkTertiary = LightTertiary
val DarkOnTertiary = LightOnTertiary

@Composable
fun shimmerBase(): Color {
    return if (isSystemInDarkTheme()) Color(0xFF2F2A35) else Color(0xFFE8E0EB)
}

@Composable
fun shimmerOverlay(): Color {
    return if (isSystemInDarkTheme()) Color(0xFF3D3745) else Color(0xFFF1EAF3)
}
