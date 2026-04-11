package io.github.v2compose.ui.main

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.v2compose.core.CheckInWorker
import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.util.WebViewProxy
import java.time.Duration
import java.util.concurrent.ExecutorService

private const val AutoCheckInWorkName = "autoCheckInWork"

class AndroidMainPlatformDelegate(
    private val context: Context,
    private val appExecutorService: ExecutorService,
) : MainPlatformDelegate {

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

    override fun updateWebViewProxy(proxyInfo: ProxyInfo) {
        WebViewProxy.updateProxy(proxyInfo, appExecutorService)
    }
}
