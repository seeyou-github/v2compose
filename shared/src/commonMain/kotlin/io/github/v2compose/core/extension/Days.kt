package io.github.v2compose.core.extension

import io.github.v2compose.util.currentTimeMillis

private const val dayMills = 24 * 60 * 60 * 1000

fun Long.isBeforeTodayByUTC() = newDayThan(currentTimeMillis())

fun Long.newDayThan(other: Long): Boolean {
    return this / dayMills > other / dayMills
}