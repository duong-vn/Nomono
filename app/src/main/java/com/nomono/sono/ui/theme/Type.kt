package com.nomono.sono.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nomono.sono.R

val VT323FontFamily = FontFamily(
    Font(R.font.vt323, FontWeight.Normal),
    Font(R.font.vt323, FontWeight.Medium),
    Font(R.font.vt323, FontWeight.SemiBold),
    Font(R.font.vt323, FontWeight.Bold),
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = VT323FontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = VT323FontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = VT323FontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = VT323FontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = VT323FontFamily),
    headlineSmall = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = VT323FontFamily),
    bodyLarge = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyMedium = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = VT323FontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = VT323FontFamily),
    labelMedium = TextStyle(
        fontFamily = VT323FontFamily,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = VT323FontFamily),
)
