package io.github.v2compose.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.math.roundToLong

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).roundToLong()
