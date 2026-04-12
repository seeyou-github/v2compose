package io.github.v2compose

import androidx.compose.runtime.compositionLocalOf

/**
 * Shared platform interaction handlers exposed to common UI.
 */
data class AppPlatformHandlers(
    val capabilities: PlatformCapabilities,
    val openExternalUri: (String) -> Unit,
    val shareContent: (String, String) -> Unit,
    val saveImage: (String) -> Unit,
    val openAppSettings: () -> Unit,
    val openNotificationSettings: () -> Unit,
    val copyToClipboard: (String) -> Unit,
    val checkNotificationPermission: () -> Boolean,
    val isAutoCheckInChannelEnabled: () -> Boolean,
    val requestNotificationPermission: () -> Unit,
)

val LocalAppPlatformHandlers = compositionLocalOf<AppPlatformHandlers> {
    error("LocalAppPlatformHandlers not provided")
}
