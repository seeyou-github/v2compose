package io.github.v2compose.network

import android.webkit.CookieManager as AndroidCookieManager
import io.github.v2compose.util.KLogger
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.IOException
import java.net.URI

class WebkitCookieManager : CookieJar, CookieManager {
    private val cookieManager = AndroidCookieManager.getInstance()

    fun put(uri: URI?, responseHeaders: Map<String, List<String>>?) {
        if (uri == null || responseHeaders == null) return

        val url = uri.toString()
        responseHeaders.forEach { (headerKey, headerValues) ->
            if (headerKey == null) return@forEach
            if (!headerKey.equals("Set-Cookie2", ignoreCase = true) &&
                !headerKey.equals("Set-Cookie", ignoreCase = true)
            ) {
                return@forEach
            }

            headerValues.forEach { headerValue ->
                cookieManager.setCookie(url, headerValue)
            }
        }
    }

    fun get(uri: URI?, requestHeaders: Map<String, List<String>>?): Map<String, List<String>> {
        require(uri != null && requestHeaders != null) { "Argument is null" }

        val cookie = cookieManager.getCookie(uri.toString()) ?: return emptyMap()
        return mapOf("Cookie" to listOf(cookie))
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val responseHeaders = mapOf("Set-Cookie" to cookies.map(Cookie::toString))
        runCatching {
            put(url.toUri(), responseHeaders)
        }.onFailure { throwable ->
            KLogger.e("WebkitCookieManager", "saveFromResponse error", throwable)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return runCatching {
            get(url.toUri(), emptyMap()).values
                .flatten()
                .flatMap { cookieHeader ->
                    cookieHeader.split(';').mapNotNull { cookie ->
                        Cookie.parse(url, cookie.trim())
                    }
                }
        }.onFailure { throwable ->
            KLogger.e("WebkitCookieManager", "loadForRequest error", throwable)
        }.getOrDefault(emptyList())
    }

    override fun clearCookies() {
        cookieManager.removeAllCookies(null)
    }
}
