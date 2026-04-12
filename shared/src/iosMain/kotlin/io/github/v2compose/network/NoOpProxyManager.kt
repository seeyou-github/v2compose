package io.github.v2compose.network

import io.github.v2compose.shared.bean.ProxyInfo

class NoOpProxyManager : ProxyManager {
    override fun updateProxy(proxyInfo: ProxyInfo) = Unit
}
