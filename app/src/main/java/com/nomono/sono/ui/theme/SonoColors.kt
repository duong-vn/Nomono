package com.nomono.sono.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class SonoColors(
    val oweMe: Color,
    val oweMeContainer: Color,
    val onOweMeContainer: Color,
    val iOwe: Color,
    val iOweContainer: Color,
    val onIOweContainer: Color,
    val backgroundGradient: Brush,
    val glassColor: Color,
    val glassBorder: Color,
    val glassGoldBorder: Color,
)

val TerminalSonoColors = SonoColors(
    oweMe = TerminalOrange,
    oweMeContainer = Color(0xFF221105),
    onOweMeContainer = TerminalInk,
    iOwe = TerminalNeg,
    iOweContainer = Color(0xFF290A07),
    onIOweContainer = Color(0xFFFF9E94),
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(TerminalPage, TerminalScreen),
    ),
    glassColor = TerminalScreen,
    glassBorder = TerminalOrange,
    glassGoldBorder = TerminalOrangeDim,
)

val LightSonoColors = SonoColors(
    oweMe = LokiGreenDark,
    oweMeContainer = LokiGreenContainerLight,
    onOweMeContainer = Color(0xFF003816),
    iOwe = Color(0xFFD32F2F),
    iOweContainer = Color(0xFFFFEBEE),
    onIOweContainer = Color(0xFFC62828),
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(LightGradientStart, LightGradientMid, LightGradientEnd),
    ),
    glassColor = LightGlassColor,
    glassBorder = LightGlassBorder,
    glassGoldBorder = Color(0x40C68A00),
)

val DarkSonoColors = SonoColors(
    oweMe = LokiGreen,
    oweMeContainer = LokiGreenContainer,
    onOweMeContainer = LokiGreenLight,
    iOwe = LokiRed,
    iOweContainer = LokiRedContainer,
    onIOweContainer = LokiRedLight,
    backgroundGradient = Brush.verticalGradient(
        colors = listOf(DarkGradientStart, DarkGradientMid, DarkGradientEnd),
    ),
    glassColor = DarkGlassColor,
    glassBorder = DarkGlassBorder,
    glassGoldBorder = DarkGlassGoldBorder,
)
