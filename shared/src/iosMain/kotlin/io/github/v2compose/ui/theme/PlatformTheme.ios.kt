package io.github.v2compose.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformTheme(
    darkTheme: Boolean,
    androidTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (ColorScheme) -> Unit
) {
    val colorScheme = when {
        androidTheme -> if (darkTheme) DarkAndroidColorScheme else LightAndroidColorScheme
        darkTheme -> DarkDefaultColorScheme
        else -> LightDefaultColorScheme
    }

    content(colorScheme)
}
