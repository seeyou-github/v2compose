package io.github.v2compose.ui.webview.client

import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator
import io.github.v2compose.Constants
import io.ktor.http.Url

private val internalPaths = setOf("t", "member", "go")

class V2exRequestInterceptor(private val openUri: (String) -> Unit) : RequestInterceptor {
    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {
        val url = request.url
        val parsedUrl = runCatching { Url(url) }.getOrNull()
        val shouldOpenInternally =
            parsedUrl?.host?.endsWith(Constants.host) == true &&
                parsedUrl.segments.firstOrNull()?.lowercase() in internalPaths
        if (shouldOpenInternally) {
            openUri(url)
            return WebRequestInterceptResult.Reject
        }
        return WebRequestInterceptResult.Allow
    }
}
