package io.github.v2compose.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

fun appTypography(primarySp: TextUnit, secondarySp: TextUnit): Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = primarySp,
        lineHeight = (primarySp.value * 1.45f).sp,
        letterSpacing = 0.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = (primarySp.value * 0.92f).sp,
        lineHeight = (primarySp.value * 1.35f).sp,
        letterSpacing = 0.2.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (primarySp.value * 1.02f).sp,
        lineHeight = (primarySp.value * 1.25f).sp,
        letterSpacing = 0.1.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (primarySp.value * 1.15f).sp,
        lineHeight = (primarySp.value * 1.3f).sp,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = secondarySp,
        lineHeight = (secondarySp.value * 1.35f).sp,
        letterSpacing = 0.1.sp,
    ),
)
