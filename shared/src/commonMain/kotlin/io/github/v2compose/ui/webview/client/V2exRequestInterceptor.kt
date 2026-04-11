package io.github.v2compose.ui.webview.client

import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator

class V2exRequestInterceptor(private val openUri: (String) -> Unit) : RequestInterceptor {
    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {
        val url = request.url
        if (url.startsWith("https://www.v2ex.com/t/") ||
            url.startsWith("https://www.v2ex.com/member/") ||
            url.startsWith("https://www.v2ex.com/go/")
        ) {
            openUri(url)
            return WebRequestInterceptResult.Reject
        }
        return WebRequestInterceptResult.Allow
    }
}
