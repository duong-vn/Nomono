package com.nomono.sono.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.nomono.sono.data.ThemeMode

private val TerminalColorScheme = darkColorScheme(
    primary = TerminalOrange,
    onPrimary = TerminalScreen,
    primaryContainer = Color(0xFF2B1405),
    onPrimaryContainer = TerminalInk,
    secondary = TerminalOrange,
    onSecondary = TerminalScreen,
    secondaryContainer = Color(0xFF2B1405),
    onSecondaryContainer = TerminalInk,
    background = TerminalPage,
    onBackground = TerminalOrange,
    surface = TerminalScreen,
    onSurface = TerminalOrange,
    surfaceVariant = Color(0xFF14171E),
    onSurfaceVariant = TerminalOrangeDim,
    surfaceContainerLowest = Color(0xFF020304),
    surfaceContainerLow = Color(0xFF07080B),
    surfaceContainer = Color(0xFF0B0D12),
    surfaceContainerHigh = Color(0xFF10131A),
    surfaceContainerHighest = Color(0xFF181D26),
    outline = TerminalOrangeDim,
    outlineVariant = Color(0xFF4A250D),
    error = TerminalNeg,
    onError = Color(0xFF330805),
)

private val LightColorScheme = lightColorScheme(
    primary = LokiGreenDark,
    onPrimary = Color.White,
    primaryContainer = LokiGreenContainerLight,
    onPrimaryContainer = LokiGreenDark,
    secondary = LokiGoldDark,
    onSecondary = Color.White,
    secondaryContainer = LokiGoldContainerLight,
    onSecondaryContainer = Color(0xFF261900),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

val LocalSonoColors = staticCompositionLocalOf { TerminalSonoColors }

@Composable
fun SonoTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.TERMINAL -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val (colorScheme, sonoColors) = if (isDark) {
        TerminalColorScheme to TerminalSonoColors
    } else {
        LightColorScheme to LightSonoColors
    }

    CompositionLocalProvider(LocalSonoColors provides sonoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                LocalTextStyle provides TextStyle(fontFamily = VT323FontFamily),
                content = content,
            )
        }
    }
}
