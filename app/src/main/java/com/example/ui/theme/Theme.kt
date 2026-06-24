package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldSidogiri,
    secondary = DarkSidogiri,
    tertiary = GoldFalak,
    background = CharcoalText,
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = CharcoalText,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = DarkSidogiri,
    onPrimaryContainer = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldSidogiri,
    secondary = DarkSidogiri,
    tertiary = GoldFalak,
    background = SlateBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = CharcoalText,
    onBackground = CharcoalText,
    onSurface = CharcoalText,
    primaryContainer = MintSoft,
    onPrimaryContainer = EmeraldSidogiri,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
