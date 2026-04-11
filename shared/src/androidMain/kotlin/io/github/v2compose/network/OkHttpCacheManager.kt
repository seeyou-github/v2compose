package io.github.v2compose.network

import okhttp3.Cache

class OkHttpCacheManager(private val cache: Cache) : HttpCacheManager {
    override val size: Long
        get() = cache.size()

    override fun clear() {
        cache.evictAll()
    }
}
