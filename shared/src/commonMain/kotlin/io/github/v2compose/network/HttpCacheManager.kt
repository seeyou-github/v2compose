package io.github.v2compose.network

interface HttpCacheManager {
    val size: Long
    fun clear()
}
