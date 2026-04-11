package io.github.v2compose.ui.main

import io.github.v2compose.shared.bean.ProxyInfo

/**
 * Platform hooks required by the main entry flow.
 */
interface MainPlatformDelegate {
    fun syncAutoCheckIn(enabled: Boolean)

    fun updateWebViewProxy(proxyInfo: ProxyInfo)
}
