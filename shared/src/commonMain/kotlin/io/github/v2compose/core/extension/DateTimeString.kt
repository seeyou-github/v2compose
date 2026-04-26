package io.github.v2compose.core.extension

import io.github.v2compose.util.currentTimeMillis
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.just_now
import v2compose.shared.generated.resources.n_days_ago
import v2compose.shared.generated.resources.n_hours_ago
import v2compose.shared.generated.resources.n_minutes_ago

private const val UTC_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
private const val UTC_TIME_LENGTH = 19

fun String.toDateTimeMillis(): Long? = parseUtcDateTimeMillis(this, UTC_TIME_PATTERN)

@androidx.compose.runtime.Composable
fun String.toTimeText(): String {
    val timeMillis = toDateTimeMillis() ?: return this

    val timeDelta = currentTimeMillis() - timeMillis
    val minMillis = 60 * 1000L
    val hourMillis = 60 * minMillis
    val dayMillis = 24 * hourMillis

    if (timeDelta >= 8 * dayMillis) {
        return formatLocalDate(timeMillis)
    }
    if (timeDelta >= dayMillis) {
        return stringResource(Res.string.n_days_ago, timeDelta / dayMillis)
    }
    if (timeDelta >= hourMillis) {
        return stringResource(Res.string.n_hours_ago, timeDelta / hourMillis)
    }
    if (timeDelta >= minMillis) {
        return stringResource(Res.string.n_minutes_ago, timeDelta / minMillis)
    }
    return stringResource(Res.string.just_now)
}

internal fun parseUtcDateTimeMillis(value: String, pattern: String): Long? {
    if (pattern != UTC_TIME_PATTERN || value.length != UTC_TIME_LENGTH) return null
    if (value[4] != '-' || value[7] != '-' || value[10] != 'T' || value[13] != ':' || value[16] != ':') {
        return null
    }

    val year = value.substring(0, 4).toIntOrNull() ?: return null
    val month = value.substring(5, 7).toIntOrNull() ?: return null
    val day = value.substring(8, 10).toIntOrNull() ?: return null
    val hour = value.substring(11, 13).toIntOrNull() ?: return null
    val minute = value.substring(14, 16).toIntOrNull() ?: return null
    val second = value.substring(17, 19).toIntOrNull() ?: return null

    if (month !in 1..12 || day !in 1..31 || hour !in 0..23 || minute !in 0..59 || second !in 0..59) {
        return null
    }

    val maxDay = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (day > maxDay) return null

    val epochDays = daysFromCivil(year, month, day)
    val secondsOfDay = hour * 3600L + minute * 60L + second
    return (epochDays * 86_400L + secondsOfDay) * 1000L
}

internal expect fun formatLocalDate(timeMillis: Long): String

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    var adjustedYear = year.toLong()
    val adjustedMonth = month.toLong()
    adjustedYear -= if (adjustedMonth <= 2L) 1 else 0
    val era = if (adjustedYear >= 0) {
        adjustedYear / 400
    } else {
        (adjustedYear - 399) / 400
    }
    val yearOfEra = adjustedYear - era * 400
    val dayOfYear = (153 * (adjustedMonth + if (adjustedMonth > 2L) -3 else 9) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097 + dayOfEra - 719_468
}
