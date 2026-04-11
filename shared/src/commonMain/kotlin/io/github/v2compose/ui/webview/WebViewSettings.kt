package io.github.v2compose.ui.webview

import com.multiplatform.webview.setting.WebSettings

internal fun WebSettings.applyBaseV2WebSettings() {
    isJavaScriptEnabled = true
    allowUniversalAccessFromFileURLs = true
    supportZoom = true
    androidWebSettings.domStorageEnabled = true
    androidWebSettings.useWideViewPort = true
}

internal fun WebSettings.applyGoogleLoginWebSettings() {
    applyBaseV2WebSettings()
    customUserAgentString = googleLoginUserAgent()
}

internal expect fun googleLoginUserAgent(): String?
