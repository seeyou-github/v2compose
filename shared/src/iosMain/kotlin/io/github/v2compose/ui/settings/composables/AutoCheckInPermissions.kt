package io.github.v2compose.ui.settings.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

@Composable
actual fun checkAndRequestNotificationPermission(
    showRationale: () -> Unit,
    onDenied: () -> Unit,
    onGranted: () -> Unit,
) {
    LaunchedEffect(Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        if (hasNotificationPermission(center)) {
            onGranted()
            return@LaunchedEffect
        }

        if (requestNotificationPermission(center)) {
            onGranted()
        } else {
            onDenied()
        }
    }
}

private suspend fun hasNotificationPermission(
    center: UNUserNotificationCenter,
): Boolean = suspendCancellableCoroutine { continuation ->
    center.getNotificationSettingsWithCompletionHandler { settings ->
        continuation.resume(
            settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional ||
                settings?.authorizationStatus == UNAuthorizationStatusEphemeral
        )
    }
}

private suspend fun requestNotificationPermission(
    center: UNUserNotificationCenter,
): Boolean = suspendCancellableCoroutine { continuation ->
    center.requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionBadge or
            UNAuthorizationOptionSound,
    ) { granted, _ ->
        continuation.resume(granted)
    }
}
