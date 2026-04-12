package io.github.v2compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.github.v2compose.ui.common.keyboardAsState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SharedApp(
    platformHandlersProvider: @Composable (SnackbarHostState) -> AppPlatformHandlers,
    androidTheme: Boolean = false,
    keyboardVisible: Boolean? = null,
    viewModel: V2AppViewModel = koinViewModel(),
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val platformHandlers = platformHandlersProvider(snackbarHostState)
    val appState = rememberV2AppState(
        navHostController = navController,
        platformHandlers = platformHandlers,
        snackbarHostState = snackbarHostState,
    )
    val appKeyboardVisible = keyboardVisible ?: keyboardAsState().value

    V2AppShell(
        appSettings = appSettings,
        snackbarHostState = snackbarHostState,
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
