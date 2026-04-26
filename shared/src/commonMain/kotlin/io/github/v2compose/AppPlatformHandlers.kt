package io.github.v2compose

import androidx.compose.runtime.compositionLocalOf

fun interface ExternalNavigator {
    fun openExternalUri(uri: String)
}

fun interface ShareLauncher {
    fun shareContent(title: String, url: String)
}

fun interface ImageSaver {
    fun saveImage(url: String)
}

interface SettingsLauncher {
    fun openAppSettings()

    fun openNotificationSettings()
}

interface NotificationAccess {
    fun hasNotificationPermission(): Boolean

    fun isAutoCheckInChannelEnabled(): Boolean
}

enum class AutoCheckInPrerequisiteState {
    Ready,
    RequiresNotificationPermission,
    RequiresNotificationSettings,
}

fun interface AutoCheckInPrerequisite {
    fun check(): AutoCheckInPrerequisiteState
}

/**
 * Shared platform interaction handlers exposed to common UI.
 */
data class AppPlatformHandlers(
    val capabilities: PlatformCapabilities,
    val externalNavigator: ExternalNavigator,
    val shareLauncher: ShareLauncher,
    val imageSaver: ImageSaver,
    val settingsLauncher: SettingsLauncher,
    val notificationAccess: NotificationAccess,
    val autoCheckInPrerequisite: AutoCheckInPrerequisite,
) {
    fun openExternalUri(uri: String) = externalNavigator.openExternalUri(uri)

    fun shareContent(title: String, url: String) = shareLauncher.shareContent(title, url)

    fun saveImage(url: String) = imageSaver.saveImage(url)

    fun openAppSettings() = settingsLauncher.openAppSettings()

    fun openNotificationSettings() = settingsLauncher.openNotificationSettings()

    fun checkNotificationPermission(): Boolean = notificationAccess.hasNotificationPermission()

    fun isAutoCheckInChannelEnabled(): Boolean =
        notificationAccess.isAutoCheckInChannelEnabled()

    fun checkAutoCheckInPrerequisite(): AutoCheckInPrerequisiteState =
        autoCheckInPrerequisite.check()
}

val LocalAppPlatformHandlers = compositionLocalOf<AppPlatformHandlers> {
    error("LocalAppPlatformHandlers not provided")
}
