package io.github.v2compose

import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.usecase.CheckInUseCase
import io.github.v2compose.util.KLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate

const val IosAutoCheckInTaskIdentifier = "io.github.v2compose.auto-check-in"

private const val AutoCheckInRefreshIntervalSeconds = 12.0 * 60.0 * 60.0
private const val Tag = "IosBackgroundTasks"

private var didRegisterBackgroundTask = false

@OptIn(ExperimentalForeignApi::class)
fun registerIosBackgroundTasks() {
    if (didRegisterBackgroundTask) return

    didRegisterBackgroundTask = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = IosAutoCheckInTaskIdentifier,
        usingQueue = null,
    ) { task ->
        task?.let(::handleAutoCheckInTask)
    }
}

fun syncIosAutoCheckInScheduleFromStoredSettings() {
    runIosRuntimeTask { koin ->
        val accountRepository = koin.get<AccountRepository>()
        val appPreferences = koin.get<AppPreferences>()
        val shouldSchedule = accountRepository.isLoggedIn.first() &&
            appPreferences.appSettings.first().autoCheckIn

        if (shouldSchedule) {
            scheduleIosAutoCheckInBackgroundRefresh()
        } else {
            cancelIosAutoCheckInBackgroundRefresh()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun scheduleIosAutoCheckInBackgroundRefresh() {
    cancelIosAutoCheckInBackgroundRefresh()
    val request = BGAppRefreshTaskRequest(identifier = IosAutoCheckInTaskIdentifier).apply {
        earliestBeginDate = NSDate(
            timeIntervalSinceReferenceDate = NSDate().timeIntervalSinceReferenceDate +
                AutoCheckInRefreshIntervalSeconds,
        )
    }
    runCatching {
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
    }.onFailure {
        KLogger.e(Tag, "Failed to submit iOS auto check-in task: ${it.message}", it)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun cancelIosAutoCheckInBackgroundRefresh() {
    BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(IosAutoCheckInTaskIdentifier)
}

private fun handleAutoCheckInTask(task: BGTask) {
    val refreshTask = task as? BGAppRefreshTask ?: run {
        task.setTaskCompletedWithSuccess(false)
        return
    }

    scheduleIosAutoCheckInBackgroundRefresh()

    val job = Job()
    val scope = CoroutineScope(SupervisorJob(job) + Dispatchers.Default)
    refreshTask.expirationHandler = {
        scope.cancel("iOS auto check-in background task expired.")
        refreshTask.setTaskCompletedWithSuccess(false)
    }

    scope.launch {
        val success = runAutoCheckInIfNeeded()
        refreshTask.setTaskCompletedWithSuccess(success)
    }
}

private suspend fun runAutoCheckInIfNeeded(): Boolean {
    initIosRuntime()
    val koin = KoinPlatformTools.defaultContext().get()
    val accountRepository = koin.get<AccountRepository>()
    val appPreferences = koin.get<AppPreferences>()
    if (!accountRepository.isLoggedIn.first() || !appPreferences.appSettings.first().autoCheckIn) {
        return true
    }

    return runCatching {
        koin.get<CheckInUseCase>().invoke().success
    }.getOrElse {
        KLogger.e(Tag, "iOS auto check-in task failed: ${it.message}", it)
        false
    }
}

private fun runIosRuntimeTask(block: suspend (org.koin.core.Koin) -> Unit) {
    initIosRuntime()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    scope.launch {
        val koin = KoinPlatformTools.defaultContext().get()
        block(koin)
    }
}
