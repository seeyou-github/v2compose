package io.github.v2compose.ui.webview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation
import io.ktor.http.encodeURLParameter

internal const val argsUrl = "url"
const val webViewNavigationRoute = "/webview?$argsUrl={$argsUrl}"

fun NavController.navigateToWebView(url: String) {
    val encodeUrl = url.encodeURLParameter()
    navigate("/webview?url=$encodeUrl")
}

fun NavGraphBuilder.webViewScreen(onCloseClick: () -> Unit, openUri: (String) -> Unit) {
    composableWithAnimation(
        webViewNavigationRoute,
        arguments = listOf(navArgument(argsUrl) { type = NavType.StringType })
    ) {
        WebViewScreenRoute(onCloseClick = onCloseClick, openUri = openUri)
    }
}
