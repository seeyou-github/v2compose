package io.github.v2compose.ui.appearance

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import io.github.v2compose.shared.bean.AppSettings

fun appearancePresetsFor(settings: AppSettings): List<AppearancePreset> =
    if (settings.darkThemeEnabled) DarkAppearancePresets else LightAppearancePresets

fun resolveAppearancePreset(settings: AppSettings): AppearancePreset {
    val presets = appearancePresetsFor(settings)
    val index = if (settings.darkThemeEnabled) settings.appearanceDarkPresetIndex else settings.appearanceLightPresetIndex
    return presets.getOrElse(index) { presets.first() }
}

fun resolveEffectiveColors(settings: AppSettings): AppearancePreset {
    val base = resolveAppearancePreset(settings)
    val overridesJson = if (settings.darkThemeEnabled) settings.appearanceDarkOverridesJson else settings.appearanceLightOverridesJson
    val overrides = parseOverrides(overridesJson)
    return base.copy(
        primaryText = overrides.primaryTextArgb?.let(::colorFromArgbInt) ?: base.primaryText,
        secondaryText = overrides.secondaryTextArgb?.let(::colorFromArgbInt) ?: base.secondaryText,
        primaryBackground = overrides.primaryBackgroundArgb?.let(::colorFromArgbInt) ?: base.primaryBackground,
        secondaryBackground = overrides.secondaryBackgroundArgb?.let(::colorFromArgbInt) ?: base.secondaryBackground,
        accent = overrides.accentArgb?.let(::colorFromArgbInt) ?: base.accent,
    )
}

fun appearanceColorScheme(settings: AppSettings): ColorScheme {
    val c = resolveEffectiveColors(settings)
    val dark = settings.darkThemeEnabled
    val onPrimary = if (c.accent.luminance() > 0.6f) Color(0xFF111111) else Color.White
    val outline = if (dark) c.secondaryBackground.copy(alpha = 0.8f) else Color(0xFFE1E6EE)

    return if (dark) {
        darkColorScheme(
            primary = c.accent,
            onPrimary = onPrimary,
            secondary = c.accent,
            onSecondary = onPrimary,
            background = c.primaryBackground,
            onBackground = c.primaryText,
            surface = c.primaryBackground,
            onSurface = c.primaryText,
            surfaceVariant = c.secondaryBackground,
            onSurfaceVariant = c.secondaryText,
            outline = outline,
            outlineVariant = outline,
        )
    } else {
        lightColorScheme(
            primary = c.accent,
            onPrimary = onPrimary,
            secondary = c.accent,
            onSecondary = onPrimary,
            background = c.primaryBackground,
            onBackground = c.primaryText,
            surface = c.primaryBackground,
            onSurface = c.primaryText,
            surfaceVariant = c.secondaryBackground,
            onSurfaceVariant = c.secondaryText,
            outline = outline,
            outlineVariant = outline,
        )
    }
}
