package io.github.v2compose.ui.main.mine

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import io.github.v2compose.LocalSnackbarHostState
import io.github.v2compose.shared.bean.Account
import io.github.v2compose.ui.BaseScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.*

@Composable
fun rememberMineContentState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
): MineContentState {
    return remember(context, coroutineScope, snackbarHostState) {
        MineContentState(context, coroutineScope, snackbarHostState)
    }
}

class MineContentState(
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) : BaseScreenState(context, coroutineScope, snackbarHostState) {

    fun notImplemented() {
        coroutineScope.launch {
            val message = getString(Res.string.function_not_implemented)
            snackbarHostState.showSnackbar(message = message)
        }
    }

    fun doActionIfLoggedIn(account: Account, action: () -> Unit) {
        if (account.isValid()) {
            action()
        } else {
            coroutineScope.launch {
                showMessage(getString(Res.string.login_first))
            }
        }
    }

}