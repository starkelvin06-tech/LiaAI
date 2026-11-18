package com.Lia.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Design Tokens: "A Pearl of Light in the Night"
val IndigoNight = Color(0xFF0A0A2E)    // Deep Indigo Night background
val IndigoMid = Color(0xFF12124A)      // Surface
val IndigoSurface = Color(0xFF1A1A5E)  // Elevated Surface Variant
val Marigold = Color(0xFFFFA500)       // Primary Accent
val MarigoldDim = Color(0xFFCC8400)    // Secondary Accent
val PearlWhite = Color(0xFFF5F5FF)     // Glowing Pearl center & highlight
val TextPrimary = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFFAAAACC)
val OutlineColor = Color(0xFF2C2C6A)
val ErrorRed = Color(0xFFFF5252)

val DarkColorScheme = darkColorScheme(
    primary = Marigold,
    onPrimary = IndigoNight,
    primaryContainer = MarigoldDim,
    onPrimaryContainer = PearlWhite,
    secondary = MarigoldDim,
    onSecondary = IndigoNight,
    background = IndigoNight,
    onBackground = TextPrimary,
    surface = IndigoMid,
    onSurface = TextPrimary,
    surfaceVariant = IndigoSurface,
    onSurfaceVariant = TextSecondary,
    outline = OutlineColor,
    error = ErrorRed,
    onError = PearlWhite
)

val LightColorScheme = lightColorScheme(
    primary = MarigoldDim,
    onPrimary = PearlWhite,
    primaryContainer = Marigold,
    onPrimaryContainer = IndigoNight,
    secondary = Marigold,
    onSecondary = IndigoNight,
    background = PearlWhite,
    onBackground = IndigoNight,
    surface = Color(0xFFF0F0FC),
    onSurface = IndigoNight,
    surfaceVariant = Color(0xFFE2E2F8),
    onSurfaceVariant = Color(0xFF202050),
    outline = Color(0xFFB0B0D8),
    error = ErrorRed,
    onError = PearlWhite
)

@Composable
fun LiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
