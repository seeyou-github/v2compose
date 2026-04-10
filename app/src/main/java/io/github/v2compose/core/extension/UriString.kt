package io.github.v2compose.core.extension

import android.net.Uri

fun String.tryParse(): Uri? {
    return try {
        Uri.parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
