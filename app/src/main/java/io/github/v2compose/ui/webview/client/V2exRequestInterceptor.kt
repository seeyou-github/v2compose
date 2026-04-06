package io.github.v2compose.ui.webview.client

import android.net.Uri
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator

class V2exRequestInterceptor(private val openUri: (String) -> Unit) : RequestInterceptor {
    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {
        if (interceptUrl(request)) {
            return WebRequestInterceptResult.Reject
        }
        return WebRequestInterceptResult.Allow
    }

    private fun interceptUrl(request: WebRequest): Boolean {
        val uri = Uri.parse(request.url)
        uri.pathSegments?.firstOrNull()?.let {
            if (listOf("t", "go", "member").contains(it.lowercase())) {
                openUri(request.url)
                return true
            }
        }
        return false
    }

}