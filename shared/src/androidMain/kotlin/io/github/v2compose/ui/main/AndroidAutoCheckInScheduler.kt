package io.github.v2compose.ui.main

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.v2compose.core.CheckInWorker
import java.time.Duration

private const val AutoCheckInWorkName = "autoCheckInWork"

class AndroidAutoCheckInScheduler(
    private val context: Context,
) : AutoCheckInScheduler {
    override fun syncAutoCheckIn(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val checkInWorkRequest = PeriodicWorkRequestBuilder<CheckInWorker>(
                Duration.ofHours(12),
                Duration.ofHours(1),
            ).build()
            workManager.enqueueUniquePeriodicWork(
                AutoCheckInWorkName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                checkInWorkRequest,
            )
        } else {
            workManager.cancelUniqueWork(AutoCheckInWorkName)
        }
    }
}
