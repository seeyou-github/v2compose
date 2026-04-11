package io.github.v2compose

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import io.github.v2compose.core.openInBrowser
import io.github.v2compose.core.share
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.save_image_failed
import v2compose.shared.generated.resources.save_image_success
import java.io.File

@Composable
fun rememberAndroidExternalUriHandler(
    context: Context = LocalContext.current,
): (String) -> Unit = remember(context) {
    { uri -> context.openExternalUri(uri) }
}

@Composable
fun rememberAndroidShareHandler(
    context: Context = LocalContext.current,
): (String, String) -> Unit = remember(context) {
    { title, url -> context.share(title, url) }
}

@Composable
fun rememberAndroidImageSaver(
    snackbarHostState: SnackbarHostState,
    context: Context = LocalContext.current,
): (String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    return remember(context, coroutineScope, snackbarHostState) {
        { url ->
            coroutineScope.launch {
                context.saveImage(url, snackbarHostState)
            }
        }
    }
}

@Composable
fun rememberAndroidAppPlatformHandlers(
    snackbarHostState: SnackbarHostState,
    context: Context = LocalContext.current,
): AppPlatformHandlers {
    val openExternalUri = rememberAndroidExternalUriHandler(context)
    val shareContent = rememberAndroidShareHandler(context)
    val saveImage = rememberAndroidImageSaver(
        snackbarHostState = snackbarHostState,
        context = context,
    )

    return remember(openExternalUri, shareContent, saveImage, context) {
        AppPlatformHandlers(
            openExternalUri = openExternalUri,
            shareContent = shareContent,
            saveImage = saveImage,
            openAppSettings = { context.openAppSettings() },
            openNotificationSettings = { context.openNotificationSettings() },
            copyToClipboard = { text -> context.copyToClipboard(text) },
            checkNotificationPermission = { context.checkNotificationPermission() },
            isAutoCheckInChannelEnabled = { io.github.v2compose.core.NotificationCenter.isAutoCheckInChannelEnabled(context) },
            requestNotificationPermission = { context.openNotificationSettings() }
        )
    }
}

private fun Context.openNotificationSettings() {
    val intent = Intent().apply {
        action = "android.settings.APP_NOTIFICATION_SETTINGS"
        putExtra("android.provider.extra.APP_PACKAGE", packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun Context.openExternalUri(uri: String) {
    val parsedUri = runCatching { Uri.parse(uri) }.getOrNull() ?: return
    when (parsedUri.scheme) {
        "mailto", "sms", "tel" -> startActivity(Intent(Intent.ACTION_VIEW, parsedUri))
        else -> openInBrowser(uri, true)
    }
}

@OptIn(ExperimentalCoilApi::class)
private suspend fun Context.saveImage(
    url: String,
    snackbarHostState: SnackbarHostState,
) {
    val imageName = Uri.parse(url).lastPathSegment ?: return
    val pictureDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val appImageDir = File(pictureDir, "v2compose").also { it.mkdirs() }

    val message = imageLoader.diskCache?.openSnapshot(url)?.let { snapshot ->
        val newFile = File(appImageDir, imageName)
        snapshot.data.toFile().copyTo(newFile, overwrite = true)
        snapshot.close()
        getString(Res.string.save_image_success)
    } ?: getString(Res.string.save_image_failed)

    snackbarHostState.showSnackbar(
        message = message,
        duration = SnackbarDuration.Short,
    )
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("v2compose", text)
    clipboard.setPrimaryClip(clip)
}

private fun Context.checkNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
