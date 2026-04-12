package io.github.v2compose

import androidx.compose.runtime.Composable

@Composable
fun V2App() {
    SharedApp(
        platformHandlersProvider = { snackbarHostState ->
            rememberAndroidAppPlatformHandlers(snackbarHostState)
        },
        androidTheme = true,
    )
}
