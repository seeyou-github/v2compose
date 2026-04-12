package io.github.v2compose

import androidx.compose.runtime.Composable

@Composable
fun V2App() {
    val openExternalUri = rememberAndroidExternalUriHandler()
    SharedApp(
        platformHandlersProvider = { snackbarHostState ->
            rememberAndroidAppPlatformHandlers(snackbarHostState)
        },
        openExternalUri = openExternalUri,
        androidTheme = true,
    )
}
