package io.github.v2compose.core

import androidx.compose.ui.platform.ClipEntry

expect fun plainTextClipEntry(text: String): ClipEntry
