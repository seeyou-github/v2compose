package io.github.v2compose.ui.supplement

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.v2compose.LocalSnackbarHostState
import io.github.v2compose.ui.BaseScreenState
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAddSupplementScreenState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
): AddSupplementScreenState {
    return remember(coroutineScope, snackbarHostState) {
        AddSupplementScreenState(coroutineScope, snackbarHostState)
    }
}

@Stable
class AddSupplementScreenState(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) : BaseScreenState(coroutineScope, snackbarHostState) {


}
