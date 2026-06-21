package io.github.v2compose.ui.appearance

import androidx.compose.ui.graphics.Color

// Low-contrast, iOS-like muted palettes.
val DarkAppearancePresets: List<AppearancePreset> = listOf(
    AppearancePreset(
        name = "石墨",
        primaryText = Color(0xFFE7EAF0),
        secondaryText = Color(0xFFA6AFBA),
        primaryBackground = Color(0xFF121417),
        secondaryBackground = Color(0xFF1A1E23),
        accent = Color(0xFF6AA9FF),
    ),
    AppearancePreset(
        name = "夜蓝",
        primaryText = Color(0xFFE6ECFF),
        secondaryText = Color(0xFFA7B2D0),
        primaryBackground = Color(0xFF0F1420),
        secondaryBackground = Color(0xFF171F2F),
        accent = Color(0xFF8BB7FF),
    ),
    AppearancePreset(
        name = "墨绿",
        primaryText = Color(0xFFE6F2EB),
        secondaryText = Color(0xFFA4C1B1),
        primaryBackground = Color(0xFF0F1714),
        secondaryBackground = Color(0xFF17221D),
        accent = Color(0xFF71D6A6),
    ),
    AppearancePreset(
        name = "暮紫",
        primaryText = Color(0xFFF1E8FF),
        secondaryText = Color(0xFFB9A8D6),
        primaryBackground = Color(0xFF14101B),
        secondaryBackground = Color(0xFF1D1628),
        accent = Color(0xFFB69CFF),
    ),
    AppearancePreset(
        name = "深灰",
        primaryText = Color(0xFFEAECEF),
        secondaryText = Color(0xFFAEB4BC),
        primaryBackground = Color(0xFF101214),
        secondaryBackground = Color(0xFF181B1F),
        accent = Color(0xFFFFB070),
    ),
    AppearancePreset(
        name = "可可",
        primaryText = Color(0xFFF3EEE8),
        secondaryText = Color(0xFFBEB0A1),
        primaryBackground = Color(0xFF171311),
        secondaryBackground = Color(0xFF221B17),
        accent = Color(0xFFFFC08A),
    ),
)

val LightAppearancePresets: List<AppearancePreset> = listOf(
    AppearancePreset(
        name = "雾白",
        primaryText = Color(0xFF1B1F28),
        secondaryText = Color(0xFF5B6472),
        primaryBackground = Color(0xFFF7F8FA),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF3A7BFF),
    ),
    AppearancePreset(
        name = "纸黄",
        primaryText = Color(0xFF202018),
        secondaryText = Color(0xFF6A6A57),
        primaryBackground = Color(0xFFF9F6EF),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF4A8B6E),
    ),
    AppearancePreset(
        name = "薄荷",
        primaryText = Color(0xFF12201B),
        secondaryText = Color(0xFF4E6B60),
        primaryBackground = Color(0xFFF2FAF7),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF1FA67A),
    ),
    AppearancePreset(
        name = "薰衣草",
        primaryText = Color(0xFF1E1B26),
        secondaryText = Color(0xFF625A74),
        primaryBackground = Color(0xFFF6F3FB),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF7D5CFF),
    ),
    AppearancePreset(
        name = "海盐",
        primaryText = Color(0xFF16212A),
        secondaryText = Color(0xFF566774),
        primaryBackground = Color(0xFFF3F7FA),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF2F87C8),
    ),
    AppearancePreset(
        name = "灰蓝",
        primaryText = Color(0xFF171D24),
        secondaryText = Color(0xFF5C6773),
        primaryBackground = Color(0xFFF4F6F8),
        secondaryBackground = Color(0xFFFFFFFF),
        accent = Color(0xFF566CFF),
    ),
)
