package io.github.v2compose.core

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun plainTextClipEntry(text: String): ClipEntry {
    return ClipData.newPlainText("plain text", text).toClipEntry()
}
