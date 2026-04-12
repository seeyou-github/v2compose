package io.github.v2compose.network

class NoOpHttpCacheManager : HttpCacheManager {
    override val size: Long = 0L

    override fun clear() = Unit
}
