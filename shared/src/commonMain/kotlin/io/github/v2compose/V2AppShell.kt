package io.github.v2compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.v2compose.shared.bean.AppSettings
import io.github.v2compose.shared.bean.DarkMode
import io.github.v2compose.ui.theme.V2composeTheme

val LocalSnackbarHostState =
    compositionLocalOf<SnackbarHostState> { error("LocalSnackbar not provided") }

private val BottomAppBarHeight = 72.dp

@Composable
fun V2AppShell(
    appSettings: AppSettings,
    snackbarHostState: SnackbarHostState,
    keyboardVisible: Boolean,
    platformHandlers: AppPlatformHandlers,
    androidTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appSettings.darkMode) {
        DarkMode.FollowSystem -> isSystemInDarkTheme()
        DarkMode.Off -> false
        DarkMode.On -> true
    }
    val extraPadding =
        if (keyboardVisible) Modifier.imePadding() else Modifier.padding(bottom = BottomAppBarHeight)

    V2composeTheme(androidTheme = androidTheme, darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalSnackbarHostState provides snackbarHostState,
            LocalAppPlatformHandlers provides platformHandlers,
        ) {
            Box {
                content()

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .then(extraPadding)
                )
            }
        }
    }
}
