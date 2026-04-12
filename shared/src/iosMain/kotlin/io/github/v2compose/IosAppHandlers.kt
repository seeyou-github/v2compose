package io.github.v2compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait

@Composable
fun rememberIosExternalUriHandler(): (String) -> Unit {
    return remember {
        { uri -> openUrl(uri) }
    }
}

@Composable
fun rememberIosAppPlatformHandlers(
    snackbarHostState: SnackbarHostState,
): AppPlatformHandlers {
    val viewController = LocalUIViewController.current
    val openExternalUri = rememberIosExternalUriHandler()

    return remember(viewController, openExternalUri, snackbarHostState) {
        AppPlatformHandlers(
            openExternalUri = openExternalUri,
            shareContent = { title, url ->
                presentActivitySheet(
                    viewController = viewController,
                    items = listOf(title, url),
                )
            },
            saveImage = { imageUrl ->
                val shareItem = NSURL.URLWithString(imageUrl) ?: imageUrl
                presentActivitySheet(
                    viewController = viewController,
                    items = listOf(shareItem),
                )
            },
            openAppSettings = { openUrl(UIApplicationOpenSettingsURLString) },
            openNotificationSettings = { openUrl(UIApplicationOpenSettingsURLString) },
            copyToClipboard = { text ->
                UIPasteboard.generalPasteboard.string = text
            },
            checkNotificationPermission = ::hasNotificationPermission,
            isAutoCheckInChannelEnabled = { true },
            requestNotificationPermission = ::requestNotificationPermission,
        )
    }
}

private fun presentActivitySheet(
    viewController: UIViewController,
    items: List<Any>,
) {
    val controller = UIActivityViewController(
        activityItems = items,
        applicationActivities = null,
    )
    viewController.presentViewController(controller, animated = true, completion = null)
}

private fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

private fun hasNotificationPermission(): Boolean {
    val semaphore = dispatch_semaphore_create(0)
    var granted = false
    UNUserNotificationCenter.currentNotificationCenter()
        .getNotificationSettingsWithCompletionHandler { settings ->
            granted = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized,
                UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral,
                -> true

                else -> false
            }
            dispatch_semaphore_signal(semaphore)
        }
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER)
    return granted
}

private fun requestNotificationPermission() {
    val options =
        UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
    UNUserNotificationCenter.currentNotificationCenter()
        .requestAuthorizationWithOptions(options) { _, _ -> }
}
