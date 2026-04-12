package io.github.v2compose

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initIosRuntime()
    return ComposeUIViewController {
        SharedApp(
            platformHandlersProvider = { snackbarHostState ->
                rememberIosAppPlatformHandlers(snackbarHostState)
            },
            openExternalUri = rememberIosExternalUriHandler(),
        )
    }
}
