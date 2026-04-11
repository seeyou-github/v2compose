package io.github.v2compose.ui.settings.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun checkAndRequestNotificationPermission(
    showRationale: () -> Unit,
    onDenied: () -> Unit,
    onGranted: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onGranted()
    }
}
