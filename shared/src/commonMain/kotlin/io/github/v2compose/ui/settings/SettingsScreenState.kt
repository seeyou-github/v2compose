package io.github.v2compose.ui.settings


import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.v2compose.LocalSnackbarHostState
import io.github.v2compose.network.bean.Release
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.checking_for_updates
import v2compose.shared.generated.resources.logout_success
import v2compose.shared.generated.resources.no_updates

@Composable
fun rememberSettingsScreenState(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
): SettingsScreenState {
    return remember(snackbarHostState) {
        SettingsScreenState(snackbarHostState)
    }
}

@Stable
class SettingsScreenState(
    val snackbarHostState: SnackbarHostState,
) {

    suspend fun checkForUpdates(
        checkForUpdates: suspend () -> Release,
        onNewRelease: (Release) -> Unit,
    ) = coroutineScope {
        val showSnackbar =
            async {
                snackbarHostState.showSnackbar(
                    getString(Res.string.checking_for_updates),
                    duration = SnackbarDuration.Short,
                )
            }
        val check = async { checkForUpdates() }
        val release = check.await()
        showSnackbar.cancel()
        if (release.isValid()) {
            onNewRelease(release)
        } else {
            snackbarHostState.showSnackbar(
                getString(Res.string.no_updates),
                duration = SnackbarDuration.Short,
            )
        }
    }

    suspend fun logout(logout: suspend () -> Unit) {
        logout()
        snackbarHostState.showSnackbar(
            getString(Res.string.logout_success),
            duration = SnackbarDuration.Short
        )
    }

}