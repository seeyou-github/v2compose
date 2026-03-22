package io.github.v2compose.core

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import io.github.v2compose.R
import io.github.v2compose.usecase.CheckInUseCase

private const val TAG = "CheckInWorker"
private const val NotificationIdCheckIn: Int = 1001

class CheckInWorker constructor(
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

    private fun createNotification(): Notification {
        return Notification.Builder(applicationContext, NotificationCenter.ChannelAutoCheckIn)
            .setContentTitle(applicationContext.getString(R.string.auto_checking_in))
            .build()
    }

}