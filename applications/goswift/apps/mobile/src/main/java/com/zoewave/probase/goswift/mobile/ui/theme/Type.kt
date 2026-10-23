package com.zoewave.probase.goswift.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Note: Ensure the fonts are available or use default for now with correct styling
// In a real app we'd load Space Grotesk, Manrope, and JetBrains Mono from res/font.
val HeadlineTypography = TextStyle(
    fontFamily = FontFamily.SansSerif, // Replace with Space Grotesk
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
)

val BodyTypography = TextStyle(
    fontFamily = FontFamily.SansSerif, // Replace with Manrope
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

val TelemetryTypography = TextStyle(
    fontFamily = FontFamily.Monospace, // Replace with JetBrains Mono
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
    fontFeatureSettings = "tnum"
)

val Typography = Typography(
    headlineLarge = HeadlineTypography,
    bodyLarge = BodyTypography,
    labelLarge = TelemetryTypography
)
