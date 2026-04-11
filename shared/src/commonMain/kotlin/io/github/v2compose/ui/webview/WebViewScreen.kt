package io.github.v2compose.ui.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebContent
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebStateSaver
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import io.github.v2compose.Constants
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.webview.client.V2exRequestInterceptor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.v2ex

@Composable
fun WebViewScreenRoute(
    onCloseClick: () -> Unit,
    openUri: (String) -> Unit,
    viewModel: WebViewViewModel = koinViewModel(),
) {
    WebViewScreen(url = viewModel.url, onCloseClick = onCloseClick, openUri = openUri)
}

@Composable
private fun WebViewScreen(url: String, onCloseClick: () -> Unit, openUri: (String) -> Unit) {
    val webViewState = rememberSaveableWebViewState(
        url = url,
        additionalHttpHeaders = mapOf("Refer" to Constants.baseUrl),
    )

    val loadingState = webViewState.loadingState
    val loadingProgress: Float = remember(loadingState) {
        when (loadingState) {
            is LoadingState.Initializing -> 0f
            is LoadingState.Loading -> loadingState.progress
            is LoadingState.Finished -> 1f
        }
    }

    val navigator = rememberWebViewNavigator(
        requestInterceptor = remember(openUri) {
            V2exRequestInterceptor(openUri)
        }
    )

    Scaffold(topBar = {
        WebViewTopBar(webViewState.pageTitle, onCloseClick)
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
                captureBackPresses = true
            )
            if (webViewState.isLoading) {
                LinearProgressIndicator(progress = { loadingProgress })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebViewTopBar(
    pageTitle: String?,
    onCloseClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = pageTitle ?: stringResource(Res.string.v2ex),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = { CloseButton(onCloseClick) },
    )
}

@Composable
private fun rememberSaveableWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap()
): WebViewState =
    rememberSaveable(
        url,
        additionalHttpHeaders,
        saver = WebStateSaver,
    ) {
        WebViewState(WebContent.NavigatorOnly)
    }.apply {
        content = WebContent.Url(
            url = url,
            additionalHttpHeaders = additionalHttpHeaders,
        )
        webSettings.applyBaseV2WebSettings()
    }
