package io.github.v2compose.ui.login.google

import io.github.v2compose.util.KLogger
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import io.github.v2compose.Constants
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.webview.applyGoogleLoginWebSettings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.sign_in_with_google

private const val TAG = "GoogleLogin"
private const val googleLoginUrlRefer = "${Constants.baseUrl}/signin?next=/mission/daily"

@Composable
fun GoogleLoginScreenRoute(
    onCloseClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: GoogleLoginViewModel = koinViewModel()
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    if (account.isValid()) {
        LaunchedEffect(true) {
            onLoginSuccess()
        }
    }

    val googleLoginUrl = remember(viewModel.args.once) {
        "${Constants.baseUrl}/auth/google?once=${viewModel.args.once}"
    }
    GoogleLoginScreen(
        loginUrl = googleLoginUrl,
        onCloseClick = onCloseClick,
        tryToFetchUserInfo = viewModel::fetchUserInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleLoginScreen(
    loginUrl: String,
    onCloseClick: () -> Unit,
    tryToFetchUserInfo: suspend () -> Unit,
) {
    val webViewState = rememberWebViewState(
        url = loginUrl,
        additionalHttpHeaders = mapOf("Refer" to googleLoginUrlRefer),
        extraSettings = {
            applyGoogleLoginWebSettings()
        },
    )

    val loadingState = webViewState.loadingState
    val loadingProgress: Float = remember(loadingState) {
        when (loadingState) {
            is LoadingState.Initializing -> 0f
            is LoadingState.Loading -> loadingState.progress
            is LoadingState.Finished -> 1f
        }
    }

    val fetchUserInfo by rememberUpdatedState(tryToFetchUserInfo)
    webViewState.lastLoadedUrl?.let {
        KLogger.d(TAG, "currentUrl = $it")
        if (it.startsWith("${Constants.baseUrl}/auth/google")) {
            return@let
        }
        if (it.startsWith(Constants.baseUrl)) {
            LaunchedEffect(true) {
                fetchUserInfo()
            }
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(Res.string.sign_in_with_google)) },
            navigationIcon = { CloseButton(onCloseClick) })
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                captureBackPresses = true,
            )
            if (webViewState.isLoading) {
                LinearProgressIndicator(progress = { loadingProgress })
            }
        }
    }
}
