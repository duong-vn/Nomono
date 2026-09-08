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
    oweMe = Color(0xFF2E7D32),
    oweMeContainer = Color(0xFFE5F1E7),
    onOweMeContainer = Color(0xFF1B5E20),
    iOwe = Color(0xFFC62828),
    iOweContainer = Color(0xFFFBE9E9),
    onIOweContainer = Color(0xFFB71C1C),
)

val DarkSonoColors = SonoColors(
    oweMe = Color(0xFF81C784),
    oweMeContainer = Color(0xFF1C3320),
    onOweMeContainer = Color(0xFFA5D6A7),
    iOwe = Color(0xFFE57373),
    iOweContainer = Color(0xFF3A2020),
    onIOweContainer = Color(0xFFEF9A9A),
)
