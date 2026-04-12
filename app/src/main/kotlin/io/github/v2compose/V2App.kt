package io.github.v2compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.github.v2compose.ui.common.keyboardAsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun V2App(viewModel: V2AppViewModel = koinViewModel()) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()
    val navController = rememberNavController()
    val openExternalUri = rememberAndroidExternalUriHandler()
    val appState = rememberV2AppState(
        navHostController = navController,
        openExternalUri = openExternalUri,
    )
    val platformHandlers = rememberAndroidAppPlatformHandlers(appState.snackbarHostState)

    V2AppShell(
        appSettings = appSettings,
        snackbarHostState = appState.snackbarHostState,
        keyboardVisible = keyboardState,
        platformHandlers = platformHandlers,
        androidTheme = true,
    ) {
        V2AppNavGraph(
            navController = navController,
            appState = appState,
            viewModel = viewModel,
        )
    }
}
