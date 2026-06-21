package io.github.v2compose.ui.settings

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.v2compose.shared.bean.AppSettings
import kotlin.math.roundToInt

/**
 * Represents a single theme preset with the five core color roles.
 */
data class ColorPreset(
    val name: String,
    val primaryText: Color,
    val secondaryText: Color,
    val primaryBackground: Color,
    val secondaryBackground: Color,
    val accent: Color,
)

/**
 * Six light presets (iOS-inspired moderate contrast).
 */
val LightPresets = listOf(
    ColorPreset(
        name = "经典白",
        primaryText = Color(0xFF1C1C1E),
        secondaryText = Color(0xFF8E8E93),
        primaryBackground = Color(0xFFF2F2F7),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF007AFF),
    ),
    ColorPreset(
        name = "暖灰",
        primaryText = Color(0xFF2C2C2E),
        secondaryText = Color(0xFF8E8E93),
        primaryBackground = Color(0xFFEAE5E0),
        secondaryBackground = Color(0xFFF5F0EB),
        accent = Color(0xFFAF6919),
    ),
    ColorPreset(
        name = "薄荷",
        primaryText = Color(0xFF1C2B22),
        secondaryText = Color(0xFF7A8C82),
        primaryBackground = Color(0xFFE4F0E8),
        secondaryBackground = Color(0xFFF0F8F3),
        accent = Color(0xFF34A85A),
    ),
    ColorPreset(
        name = "薰衣",
        primaryText = Color(0xFF251F35),
        secondaryText = Color(0xFF8A839A),
        primaryBackground = Color(0xFFEEEAF5),
        secondaryBackground = Color(0xFFF8F5FC),
        accent = Color(0xFF7D5DAF),
    ),
    ColorPreset(
        name = "海蓝",
        primaryText = Color(0xFF102838),
        secondaryText = Color(0xFF7A8E9F),
        primaryBackground = Color(0xFFE0ECF5),
        secondaryBackground = Color(0xFFEFF7FE),
        accent = Color(0xFF2D7BC2),
    ),
    ColorPreset(
        name = "浅粉",
        primaryText = Color(0xFF351E26),
        secondaryText = Color(0xFF9A848E),
        primaryBackground = Color(0xFFF5E7ED),
        secondaryBackground = Color(0xFFFDF4F7),
        accent = Color(0xFFC84B7A),
    ),
)

/**
 * Six dark presets (iOS-inspired moderate contrast).
 */
val DarkPresets = listOf(
    ColorPreset(
        name = "经典黑",
        primaryText = Color(0xFFE5E5EA),
        secondaryText = Color(0xFF8E8E93),
        primaryBackground = Color(0xFF000000),
        secondaryBackground = Color(0xFF1C1C1E),
        accent = Color(0xFF0A84FF),
    ),
    ColorPreset(
        name = "深空灰",
        primaryText = Color(0xFFD1D1D6),
        secondaryText = Color(0xFF8E8E93),
        primaryBackground = Color(0xFF101014),
        secondaryBackground = Color(0xFF20202A),
        accent = Color(0xFF5E5CE6),
    ),
    ColorPreset(
        name = "墨绿",
        primaryText = Color(0xFFCDE0D3),
        secondaryText = Color(0xFF7A9080),
        primaryBackground = Color(0xFF091A10),
        secondaryBackground = Color(0xFF13261A),
        accent = Color(0xFF30D158),
    ),
    ColorPreset(
        name = "深蓝",
        primaryText = Color(0xFFC8D8E8),
        secondaryText = Color(0xFF6E8AA0),
        primaryBackground = Color(0xFF0A1828),
        secondaryBackground = Color(0xFF122235),
        accent = Color(0xFF409CFF),
    ),
    ColorPreset(
        name = "深紫",
        primaryText = Color(0xFFD8CDE8),
        secondaryText = Color(0xFF8A7DA0),
        primaryBackground = Color(0xFF16102A),
        secondaryBackground = Color(0xFF201835),
        accent = Color(0xFFBF5AF2),
    ),
    ColorPreset(
        name = "深红",
        primaryText = Color(0xFFE5CCD0),
        secondaryText = Color(0xFF9A7F84),
        primaryBackground = Color(0xFF1C0A10),
        secondaryBackground = Color(0xFF28121A),
        accent = Color(0xFFFF375F),
    ),
)

/**
 * Builds a Material 3 [ColorScheme] from the current [AppSettings].
 */
@Composable
fun appearanceColorScheme(settings: AppSettings): androidx.compose.material3.ColorScheme {
    val dark = settings.darkThemeEnabled
    val index = if (dark) settings.appearanceDarkPresetIndex else settings.appearanceLightPresetIndex
    val presets = if (dark) DarkPresets else LightPresets
    val overridesJson =
        if (dark) settings.appearanceDarkOverridesJson else settings.appearanceLightOverridesJson
    val preset = presets.getOrElse(index.coerceIn(0, presets.lastIndex)) { presets[0] }
    val overrides = parseColorOverrides(overridesJson)
    return if (dark) {
        darkColorScheme(
            primary = overrides["accent"] ?: preset.accent,
            onPrimary = Color.White,
            secondary = overrides["accent"] ?: preset.accent,
            onSecondary = Color.White,
            background = overrides["primaryBackground"] ?: preset.primaryBackground,
            onBackground = overrides["primaryText"] ?: preset.primaryText,
            surface = overrides["primaryBackground"] ?: preset.primaryBackground,
            onSurface = overrides["primaryText"] ?: preset.primaryText,
            surfaceVariant = overrides["secondaryBackground"] ?: preset.secondaryBackground,
            onSurfaceVariant = overrides["secondaryText"] ?: preset.secondaryText,
            outline = (overrides["secondaryText"] ?: preset.secondaryText).copy(alpha = 0.3f),
            outlineVariant = (overrides["secondaryText"] ?: preset.secondaryText).copy(alpha = 0.15f),
        )
    } else {
        lightColorScheme(
            primary = overrides["accent"] ?: preset.accent,
            onPrimary = Color.White,
            secondary = overrides["accent"] ?: preset.accent,
            onSecondary = Color.White,
            background = overrides["primaryBackground"] ?: preset.primaryBackground,
            onBackground = overrides["primaryText"] ?: preset.primaryText,
            surface = overrides["primaryBackground"] ?: preset.primaryBackground,
            onSurface = overrides["primaryText"] ?: preset.primaryText,
            surfaceVariant = overrides["secondaryBackground"] ?: preset.secondaryBackground,
            onSurfaceVariant = overrides["secondaryText"] ?: preset.secondaryText,
            outline = (overrides["secondaryText"] ?: preset.secondaryText).copy(alpha = 0.3f),
            outlineVariant = (overrides["secondaryText"] ?: preset.secondaryText).copy(alpha = 0.15f),
        )
    }
}

// ─── Color override serialization ────────────────────────────────────────────

/**
 * Parses a simple JSON object like `{"accent":-12345,...}` into a color map.
 */
fun parseColorOverrides(json: String): Map<String, Color> {
    if (json.isBlank()) return emptyMap()
    return try {
        val result = mutableMapOf<String, Color>()
        val cleaned = json.trim().removeSurrounding("{", "}")
        cleaned.split(",").forEach { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                val key = parts[0].trim().removeSurrounding("\"", "\"")
                val value = parts[1].trim().toLongOrNull()
                if (value != null) result[key] = Color(value.toInt())
            }
        }
        result
    } catch (_: Exception) {
        emptyMap()
    }
}

/**
 * Serializes a color overrides map to a JSON string.
 */
fun serializeColorOverrides(overrides: Map<String, Color>): String {
    if (overrides.isEmpty()) return ""
    return overrides.entries.joinToString(",", prefix = "{", postfix = "}") { (k, v) ->
        "\"$k\":${v.toArgb().toLong()}"
    }
}

// ─── HSV ↔ RGB conversion ────────────────────────────────────────────────────

/**
 * Converts HSV (Hue 0-360, Saturation 0-1, Value 0-1) to [Color].
 */
fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
    val m = v - c
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = ((r + m) * 255).roundToInt().coerceIn(0, 255),
        green = ((g + m) * 255).roundToInt().coerceIn(0, 255),
        blue = ((b + m) * 255).roundToInt().coerceIn(0, 255),
    )
}

/**
 * Converts a [Color] to HSV float array [Hue, Saturation, Value].
 */
fun colorToHsv(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val d = max - min
    val h = when {
        d == 0f -> 0f
        max == r -> 60f * (((g - b) / d) % 6)
        max == g -> 60f * (((b - r) / d) + 2)
        else -> 60f * (((r - g) / d) + 4)
    }
    val s = if (max == 0f) 0f else d / max
    return floatArray3(h, s, max)
}

private fun floatArray3(a: Float, b: Float, c: Float) = FloatArray(3) { i ->
    when (i) {
        0 -> a; 1 -> b; else -> c
    }
}
