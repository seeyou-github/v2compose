package io.github.v2compose.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun PlatformTheme(
    darkTheme: Boolean,
    androidTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (ColorScheme) -> Unit
)
