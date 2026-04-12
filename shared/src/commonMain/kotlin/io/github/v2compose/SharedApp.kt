package io.github.v2compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.github.v2compose.ui.common.keyboardAsState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SharedApp(
    platformHandlersProvider: @Composable (SnackbarHostState) -> AppPlatformHandlers,
    openExternalUri: (String) -> Unit,
    androidTheme: Boolean = false,
    keyboardVisible: Boolean? = null,
    viewModel: V2AppViewModel = koinViewModel(),
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val appState = rememberV2AppState(
        navHostController = navController,
        openExternalUri = openExternalUri,
    )
    val platformHandlers = platformHandlersProvider(appState.snackbarHostState)
    val appKeyboardVisible = keyboardVisible ?: keyboardAsState().value

    V2AppShell(
        appSettings = appSettings,
        snackbarHostState = appState.snackbarHostState,
        keyboardVisible = appKeyboardVisible,
        platformHandlers = platformHandlers,
        androidTheme = androidTheme,
    ) {
        SharedAppNavGraph(
            navController = navController,
            appState = appState,
            viewModel = viewModel,
        )
    }
}
