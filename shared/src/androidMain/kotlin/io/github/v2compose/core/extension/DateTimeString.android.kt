package io.github.v2compose.core.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
internal actual fun formatLocalDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}
