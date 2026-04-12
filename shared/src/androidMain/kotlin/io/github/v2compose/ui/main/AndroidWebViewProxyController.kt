package io.github.v2compose.ui.main

import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.util.WebViewProxy
import java.util.concurrent.ExecutorService

class AndroidWebViewProxyController(
    private val appExecutorService: ExecutorService,
) : WebViewProxyController {
    override fun updateWebViewProxy(proxyInfo: ProxyInfo) {
        WebViewProxy.updateProxy(proxyInfo, appExecutorService)
    }
}
