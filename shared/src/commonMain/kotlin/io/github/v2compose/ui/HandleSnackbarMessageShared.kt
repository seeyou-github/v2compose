package io.github.v2compose.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.LocalSnackbarHostState

@Composable
fun HandleSnackbarMessage(viewModel: BaseViewModel) {
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    if (!snackbarMessage.isNullOrEmpty()) {
        LaunchedEffect(snackbarMessage) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage!!,
                duration = SnackbarDuration.Short,
            )
            viewModel.updateSnackbarMessage(null)
        }
    }
}
