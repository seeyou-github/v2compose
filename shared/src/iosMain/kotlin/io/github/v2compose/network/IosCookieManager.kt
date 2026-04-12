package io.github.v2compose.network

import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieStorage

class IosCookieManager : CookieManager {
    override fun clearCookies() {
        val storage = NSHTTPCookieStorage.sharedHTTPCookieStorage
        storage.cookies.orEmpty().forEach { cookie ->
            storage.deleteCookie(cookie as NSHTTPCookie)
        }
    }
}
