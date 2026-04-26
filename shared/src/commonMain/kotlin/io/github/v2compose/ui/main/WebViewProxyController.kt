package io.github.v2compose.ui.main

import io.github.v2compose.shared.bean.ProxyInfo

interface WebViewProxyController {
    fun updateWebViewProxy(proxyInfo: ProxyInfo)
}
