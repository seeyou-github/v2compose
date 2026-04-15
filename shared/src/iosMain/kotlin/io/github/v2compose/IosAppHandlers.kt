package io.github.v2compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.v2compose.network.NetworkClientProvider
import io.github.v2compose.usecase.ExternalImageRequestHeaders
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatformTools
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIViewController
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import kotlin.coroutines.resume
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.save_image_failed
import v2compose.shared.generated.resources.save_image_success

@Composable
private fun rememberIosExternalUriHandler(): (String) -> Unit {
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
    val coroutineScope = rememberCoroutineScope()

    return remember(viewController, openExternalUri, snackbarHostState, coroutineScope) {
        AppPlatformHandlers(
            capabilities = PlatformCapabilities.Ios,
            externalNavigator = ExternalNavigator(openExternalUri),
            shareLauncher = ShareLauncher { title, url ->
                presentActivitySheet(viewController, listOf(title, url))
            },
            imageSaver = ImageSaver { imageUrl ->
                coroutineScope.launch {
                    val message = saveImageToPhotoLibrary(imageUrl)
                    snackbarHostState.showSnackbar(message)
                }
            },
            settingsLauncher = object : SettingsLauncher {
                override fun openAppSettings() {
                    openUrl(UIApplicationOpenSettingsURLString)
                }

                override fun openNotificationSettings() {
                    openUrl(UIApplicationOpenSettingsURLString)
                }
            },
            notificationAccess = object : NotificationAccess {
                override fun hasNotificationPermission(): Boolean = hasNotificationPermission()

                override fun isAutoCheckInChannelEnabled(): Boolean = true
            },
            autoCheckInPrerequisite = AutoCheckInPrerequisite {
                AutoCheckInPrerequisiteState.Ready
            },
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

@OptIn(ExperimentalForeignApi::class)
private suspend fun saveImageToPhotoLibrary(imageUrl: String): String {
    val saved = withContext(Dispatchers.Default) {
        val sourceUrl = NSURL.URLWithString(imageUrl) ?: return@withContext false
        if (!requestPhotoLibraryPermission()) return@withContext false

        val tempFileName = sourceUrl.lastPathComponent ?: "v2compose-image"
        val tempPath = NSTemporaryDirectory().trimEnd('/') + "/$tempFileName"
        val imageBytes = runCatching {
            val networkClientProvider = KoinPlatformTools.defaultContext()
                .get()
                .get<NetworkClientProvider>()
            networkClientProvider
                .imageHttpClient()
                .get(imageUrl) {
                    ExternalImageRequestHeaders.forUrl(imageUrl).forEach { (key, value) ->
                        header(key, value)
                    }
                }
                .body<ByteArray>()
        }.getOrNull() ?: return@withContext false

        val tempFile = tempPath.toPath()
        FileSystem.SYSTEM.write(tempFile) {
            write(imageBytes)
        }

        val fileUrl = NSURL.fileURLWithPath(tempPath)
        try {
            savePhotoAsset(fileUrl)
        } finally {
            runCatching {
                FileSystem.SYSTEM.delete(tempFile, mustExist = false)
            }
        }
    }

    return if (saved) {
        getString(Res.string.save_image_success)
    } else {
        getString(Res.string.save_image_failed)
    }
}

private suspend fun requestPhotoLibraryPermission(): Boolean =
    suspendCancellableCoroutine { continuation ->
        val currentStatus = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
        if (currentStatus == PHAuthorizationStatusAuthorized ||
            currentStatus == PHAuthorizationStatusLimited
        ) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
            continuation.resume(
                status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
            )
        }
    }

private suspend fun savePhotoAsset(fileUrl: NSURL): Boolean =
    suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.sharedPhotoLibrary().performChanges({
            PHAssetChangeRequest.creationRequestForAssetFromImageAtFileURL(fileUrl)
        }) { success, _ ->
            continuation.resume(success)
        }
    }
