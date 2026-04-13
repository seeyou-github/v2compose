package io.github.v2compose.network

import platform.Foundation.NSURLCache

class IosHttpCacheManager(
    private val urlCache: NSURLCache,
) : HttpCacheManager {
    override val size: Long
        get() = urlCache.currentDiskUsage.toLong()

    override fun clear() {
        urlCache.removeAllCachedResponses()
    }
}
