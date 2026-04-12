package io.github.v2compose.ui.main

import io.github.v2compose.shared.bean.ProxyInfo

class IosMainPlatformDelegate : MainPlatformDelegate {
    override fun syncAutoCheckIn(enabled: Boolean) = Unit

    override fun updateWebViewProxy(proxyInfo: ProxyInfo) = Unit
}
