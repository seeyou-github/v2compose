package io.github.v2compose.core.extension

import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.just_now
import v2compose.shared.generated.resources.n_days_ago
import v2compose.shared.generated.resources.n_hours_ago
import v2compose.shared.generated.resources.n_minutes_ago
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val UTC_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

fun String.toDateTime(): Date? {
    return SimpleDateFormat(UTC_TIME_PATTERN).parse(this)
}


@androidx.compose.runtime.Composable
fun String.toTimeText(): String {
    val timeMills = toDateTime()?.time ?: return this

    val timeDelta = System.currentTimeMillis() - timeMills
    val minMills = 60 * 1000
    val hourMills = 60 * minMills
    val dayMills = 24 * hourMills

    if (timeDelta >= 8 * dayMills) {
        val newFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return newFormatter.format(Date(timeMills))
    }
    if (timeDelta >= dayMills) {
        return stringResource(Res.string.n_days_ago, timeDelta / dayMills)
    }
    if (timeDelta >= hourMills) {
        return stringResource(Res.string.n_hours_ago, timeDelta / hourMills)
    }
    if (timeDelta >= minMills) {
        return stringResource(Res.string.n_minutes_ago, timeDelta / minMills)
    }
    return stringResource(Res.string.just_now)
}