package io.github.v2compose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import io.github.v2compose.core.NotificationCenter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.save_image_failed
import v2compose.shared.generated.resources.save_image_success
import java.io.File
import androidx.core.net.toUri

@Composable
fun rememberAndroidAppPlatformHandlers(
    snackbarHostState: SnackbarHostState,
    context: Context = LocalContext.current,
): AppPlatformHandlers {
    val coroutineScope = rememberCoroutineScope()

    return remember(context, coroutineScope) {
        AppPlatformHandlers(
            capabilities = PlatformCapabilities.Android,
            externalNavigator = { context.openExternalUri(it) },
            shareLauncher = { title, url -> context.share(title, url) },
            imageSaver = { url ->
                coroutineScope.launch { context.saveImage(url, snackbarHostState) }
            },
            settingsLauncher = object : SettingsLauncher {
                override fun openAppSettings() {
                    context.openAppSettings()
                }

                override fun openNotificationSettings() {
                    context.openNotificationSettings()
                }
            },
            notificationAccess = object : NotificationAccess {
                override fun hasNotificationPermission(): Boolean =
                    context.checkNotificationPermission()

                override fun isAutoCheckInChannelEnabled(): Boolean =
                    NotificationCenter.isAutoCheckInChannelEnabled(context)
            },
            autoCheckInPrerequisite = {
                when {
                    !context.checkNotificationPermission() ->
                        AutoCheckInPrerequisiteState.RequiresNotificationPermission

                    !NotificationCenter.isAutoCheckInChannelEnabled(context) ->
                        AutoCheckInPrerequisiteState.RequiresNotificationSettings

                    else -> AutoCheckInPrerequisiteState.Ready
                }
            },
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
    val parsedUri = runCatching { uri.toUri() }.getOrNull() ?: return
    when (parsedUri.scheme) {
        "mailto", "sms", "tel" -> startActivity(Intent(Intent.ACTION_VIEW, parsedUri))
        else -> openInBrowser(uri)
    }
}

private fun Context.openInBrowser(url: String) {
    val uri = runCatching { url.toUri() }.getOrNull() ?: return
    val defaultBrowser = getDefaultBrowser()
    val customTabsBrowsers = getCustomTabsBrowsers()

    if (customTabsBrowsers.contains(defaultBrowser)) {
        val customTabs = CustomTabsIntent.Builder().build()
        customTabs.intent.setPackage(defaultBrowser)
        customTabs.launchUrl(this, uri)
        return
    }

    if (customTabsBrowsers.isNotEmpty()) {
        val customTabs = CustomTabsIntent.Builder().build()
        customTabs.intent.setPackage(customTabsBrowsers.first())
        customTabs.launchUrl(this, uri)
        return
    }

    startActivity(Intent(Intent.ACTION_VIEW, uri))
}

private fun Context.getDefaultBrowser(): String? {
    val intent = Intent(Intent.ACTION_VIEW, "https://www.v2ex.com".toUri())
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        ?: return null
    return resolveInfo.activityInfo?.packageName
}

private fun Context.getCustomTabsBrowsers(): List<String> {
    val activityIntent = Intent()
        .setAction(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.fromParts("http", "", null))

    val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs = ArrayList<ResolveInfo>()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent().apply {
            action = ACTION_CUSTOM_TABS_CONNECTION
            setPackage(info.activityInfo.packageName)
        }
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info)
        }
    }
    return packagesSupportingCustomTabs.map { it.activityInfo.packageName }
}

@OptIn(ExperimentalCoilApi::class)
private suspend fun Context.saveImage(
    url: String,
    snackbarHostState: SnackbarHostState,
) {
    val imageName = url.toUri().lastPathSegment ?: return
    val pictureDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val appImageDir = File(pictureDir, "v2compose").also { it.mkdirs() }

    val message = imageLoader.diskCache?.openSnapshot(url)?.let { snapshot ->
        val newFile = File(appImageDir, imageName)
        snapshot.data.toFile().copyTo(newFile, overwrite = true)
        snapshot.close()
        getString(Res.string.save_image_success)
    } ?: getString(Res.string.save_image_failed)

    snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun Context.share(title: String, url: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, url)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(Intent.createChooser(sendIntent, null))
}

private fun Context.checkNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
