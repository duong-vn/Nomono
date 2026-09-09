package com.nomono.sono.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SonoColors(
    val oweMe: Color,
    val oweMeContainer: Color,
    val onOweMeContainer: Color,
    val iOwe: Color,
    val iOweContainer: Color,
    val onIOweContainer: Color,
)

val LightSonoColors = SonoColors(
    oweMe = Color(0xFF1B873F),
    oweMeContainer = Color(0xFFE6F4EA),
    onOweMeContainer = Color(0xFF0F5224),
    iOwe = Color(0xFFC62828),
    iOweContainer = Color(0xFFFDE8E8),
    onIOweContainer = Color(0xFF8E1B1B),
)

val DarkSonoColors = SonoColors(
    oweMe = Color(0xFF81C784),
    oweMeContainer = Color(0xFF13321B),
    onOweMeContainer = Color(0xFFA5D6A7),
    iOwe = Color(0xFFEF5350),
    iOweContainer = Color(0xFF3C1414),
    onIOweContainer = Color(0xFFFFB4AB),
)

