package io.github.v2compose.ui.webview

internal actual fun googleLoginUserAgent(): String? {
    return System.getProperty("http.agent")?.replace("; wv", "")
}
