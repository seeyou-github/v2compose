package io.github.v2compose.ui.webview

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation

private const val argsUrl = "url"
const val webViewNavigationRoute = "/webview?$argsUrl={$argsUrl}"

fun NavController.navigateToWebView(url: String) {
    val encodeUrl = Uri.encode(url)
    navigate("/webview?url=$encodeUrl")
}

fun NavGraphBuilder.webViewScreen(onCloseClick: () -> Unit, openUri: (String) -> Unit) {
    composableWithAnimation(
        webViewNavigationRoute,
        arguments = listOf(navArgument(argsUrl) { type = NavType.StringType })
    ) {
        val url = Uri.decode(it.arguments?.getString(argsUrl)) ?: ""
        WebViewScreenRoute(url = url, onCloseClick = onCloseClick, openUri = openUri)
    }
}