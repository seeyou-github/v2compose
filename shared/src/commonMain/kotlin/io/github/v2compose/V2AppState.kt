package io.github.v2compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import io.github.v2compose.shared.bean.RedirectEvent
import io.github.v2compose.shared.core.V2EventManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberV2AppState(
    navHostController: NavHostController,
    openExternalUri: (String) -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    eventManager: V2EventManager = koinInject(),
): V2AppState {
    val appState = remember(
        navHostController,
        openExternalUri,
        coroutineScope,
        snackbarHostState,
        eventManager,
    ) {
        V2AppState(
            navHostController = navHostController,
            openExternalUri = openExternalUri,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            eventManager = eventManager,
        )
    }

    DisposableEffect(lifecycleOwner, appState) {
        lifecycleOwner.lifecycle.addObserver(appState)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(appState)
        }
    }
    return appState
}

class V2AppState(
    private val navHostController: NavHostController,
    private val openExternalUri: (String) -> Unit,
    private val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState,
    private val eventManager: V2EventManager,
) : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        coroutineScope.launch {
            eventManager.events.collect { event ->
                if (event is RedirectEvent) {
                    handleAction(resolveRedirectLocation(event.location))
                }
            }
        }
    }

    fun back() {
        if (navHostController.currentDestination?.route != rootNavigationRoute) {
            navHostController.popBackStack()
        }
    }

    fun openUri(uri: String) {
        handleAction(resolveOpenUri(uri))
    }

    private fun handleAction(action: AppNavigationAction) {
        when (action) {
            is AppNavigationAction.External -> openExternalUri(action.uri)
            AppNavigationAction.Ignore -> Unit
            is AppNavigationAction.Navigate -> {
                val navOptions = if (action.clearBackStackToRoot) {
                    navOptions {
                        popUpTo(rootNavigationRoute) {
                            inclusive = true
                        }
                    }
                } else {
                    null
                }
                navHostController.navigate(action.route, navOptions)
            }
        }
    }
}
