package io.github.v2compose.core.extension

import android.net.Uri
import androidx.core.net.toUri

fun String.tryParse(): Uri? {
    return try {
        this.toUri()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
