package io.github.v2compose.ui.settings.composables

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import io.github.v2compose.LocalAppPlatformHandlers

@Composable
actual fun checkAndRequestNotificationPermission(
    showRationale: () -> Unit,
    onDenied: () -> Unit,
    onGranted: () -> Unit,
) {
    val context = LocalContext.current
    val platformHandlers = LocalAppPlatformHandlers.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { result ->
        if (result) {
            if (platformHandlers.checkNotificationPermission()) {
                onGranted()
            } else {
                onDenied()
            }
        } else {
            onDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (platformHandlers.checkNotificationPermission()) {
            onGranted()
        } else {
            // Simplified for now, just launch permission request
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
