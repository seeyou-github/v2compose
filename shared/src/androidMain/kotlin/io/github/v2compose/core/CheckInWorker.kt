package io.github.v2compose.core

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import io.github.v2compose.usecase.CheckInUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.auto_checking_in

private const val TAG = "CheckInWorker"
private const val NotificationIdCheckIn: Int = 1001

class CheckInWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val checkIn: CheckInUseCase,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val result = checkIn()
        Log.d(TAG, "doWork, result = $result")
        return if (result.success) Result.success() else Result.retry()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NotificationIdCheckIn, createNotification())
    }

    private suspend fun createNotification(): Notification = withContext(Dispatchers.Default) {
        val iconId = applicationContext.resources.getIdentifier(
            "ic_launcher_round",
            "mipmap",
            applicationContext.packageName
        ).let { if (it != 0) it else android.R.drawable.ic_dialog_info }

        NotificationCompat.Builder(applicationContext, NotificationCenter.ChannelAutoCheckIn)
            .setContentTitle(getString(Res.string.auto_checking_in))
            .setSmallIcon(iconId)
            .build()
    }

}
