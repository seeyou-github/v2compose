package io.github.v2compose.core.extension

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

private const val APPLE_REFERENCE_DATE_SECONDS = 978_307_200.0

internal actual fun formatLocalDate(timeMillis: Long): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
        locale = NSLocale.currentLocale
    }
    val timeIntervalSinceReferenceDate = timeMillis.toDouble() / 1000.0 - APPLE_REFERENCE_DATE_SECONDS
    return formatter.stringFromDate(NSDate(timeIntervalSinceReferenceDate = timeIntervalSinceReferenceDate))
}
