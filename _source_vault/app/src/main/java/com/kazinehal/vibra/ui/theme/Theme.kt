package com.kazinehal.vibra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VibraAccentPrimary,
    secondary = VibraAccentSecondary,
    tertiary = VibraNeonMagenta,
    background = VibraBackground,
    surface = VibraSurface,
    surfaceVariant = VibraSurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = VibraBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun VibraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
