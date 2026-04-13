package io.github.v2compose.ui.main

import io.github.v2compose.cancelIosAutoCheckInBackgroundRefresh
import io.github.v2compose.scheduleIosAutoCheckInBackgroundRefresh

class IosAutoCheckInScheduler : AutoCheckInScheduler {
    override fun syncAutoCheckIn(enabled: Boolean) {
        if (enabled) {
            scheduleIosAutoCheckInBackgroundRefresh()
        } else {
            cancelIosAutoCheckInBackgroundRefresh()
        }
    }
}
