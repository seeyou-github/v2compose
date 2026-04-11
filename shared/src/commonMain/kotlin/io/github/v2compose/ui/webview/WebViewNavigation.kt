package io.github.v2compose.ui.webview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.composableWithAnimation
import io.ktor.http.encodeURLParameter
import org.koin.compose.koinInject

private const val argsUrl = "url"
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
        val stringDecoder: StringDecoder = koinInject()
        val url = stringDecoder.decodeString(it.arguments?.getString(argsUrl).orEmpty())
        WebViewScreenRoute(url = url, onCloseClick = onCloseClick, openUri = openUri)
    }
}
