package io.github.v2compose.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseScreenState(
    protected val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState,
) {

    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

}