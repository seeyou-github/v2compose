package io.github.v2compose.ui.settings.composables

import androidx.compose.runtime.Composable

@Composable
expect fun checkAndRequestNotificationPermission(
    showRationale: () -> Unit,
    onDenied: () -> Unit,
    onGranted: () -> Unit,
)
