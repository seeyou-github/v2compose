package io.github.v2compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.github.v2compose.shared.bean.DarkMode
import io.github.v2compose.ui.common.keyboardAsState
import io.github.v2compose.ui.theme.V2composeTheme
import org.koin.androidx.compose.koinViewModel

val LocalSnackbarHostState =
    compositionLocalOf<SnackbarHostState> { error("LocalSnackbar not provided") }

private typealias ImageSaver = (String) -> Unit

val LocalImageSaver = compositionLocalOf<ImageSaver> { error("LocalImageSaver not provided") }

private val BottomAppBarHeight = 72.dp

@Composable
fun V2App(viewModel: V2AppViewModel = koinViewModel()) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()

    val darkTheme = when (appSettings.darkMode) {
        DarkMode.FollowSystem -> isSystemInDarkTheme()
        DarkMode.Off -> false
        DarkMode.On -> true
    }

    val extraPadding =
        if (keyboardState) Modifier.imePadding() else Modifier.padding(bottom = BottomAppBarHeight)

    V2composeTheme(androidTheme = true, darkTheme = darkTheme) {
        val navController = rememberNavController()
        val appState = rememberV2AppState(navHostController = navController)

        CompositionLocalProvider(
            LocalSnackbarHostState provides appState.snackbarHostState,
            LocalImageSaver provides appState::saveImage,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                V2AppNavGraph(
                    navController = navController,
                    appState = appState,
                    viewModel = viewModel,
                )

                SnackbarHost(
                    hostState = appState.snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .then(extraPadding)
                )
            }
        }
    }
}

