package io.github.v2compose.ui.appearance

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

data class AppearancePreset(
    val name: String,
    val primaryText: Color,
    val secondaryText: Color,
    val primaryBackground: Color,
    val secondaryBackground: Color,
    val accent: Color,
)

@Serializable
data class AppearanceOverrides(
    // Store as ARGB int to keep JSON compact and stable.
    val primaryTextArgb: Int? = null,
    val secondaryTextArgb: Int? = null,
    val primaryBackgroundArgb: Int? = null,
    val secondaryBackgroundArgb: Int? = null,
    val accentArgb: Int? = null,
)
