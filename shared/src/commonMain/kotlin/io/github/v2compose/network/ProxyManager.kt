package io.github.v2compose.network

import io.github.v2compose.shared.bean.ProxyInfo

interface ProxyManager {
    fun updateProxy(proxyInfo: ProxyInfo)
}
