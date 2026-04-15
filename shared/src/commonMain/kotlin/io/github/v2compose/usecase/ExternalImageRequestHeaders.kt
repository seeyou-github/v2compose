package io.github.v2compose.usecase

import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import io.github.v2compose.Constants
import io.github.v2compose.core.extension.fullUrl
import io.ktor.http.HttpHeaders
import io.ktor.http.Url

private val imgurPageHosts = setOf("imgur.com", "www.imgur.com", "m.imgur.com")
private val imgurDirectHosts = setOf("i.imgur.com")
private val imgurHosts = imgurPageHosts + imgurDirectHosts
private val supportedImgurImageExtensions = setOf("png", "jpg", "jpeg", "webp", "gif", "avif")
private val unsupportedImgurMediaExtensions = setOf("gifv", "mp4", "webm")

private const val imgurReferer = "https://imgur.com/"
private const val standardBrowserUserAgent =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
private const val imageAcceptHeader = "image/avif,image/webp,image/apng,image/*,*/*;q=0.8"
private const val probeAcceptHeader = "$imageAcceptHeader,text/html;q=0.7"
private const val refererHeader = "Referer"

object ExternalImageRequestHeaders {
    fun forUrl(url: String, probe: Boolean = false): Map<String, String> {
        val parsed = parseUrl(url) ?: return emptyMap()
        if (!parsed.host.isImgurHost()) return emptyMap()
        return linkedMapOf<String, String>(
            HttpHeaders.UserAgent to standardBrowserUserAgent,
            HttpHeaders.Accept to if (probe) probeAcceptHeader else imageAcceptHeader,
            refererHeader to imgurReferer,
        )
    }
}

internal fun applyExternalImageRequestHeaders(
    builder: ImageRequest.Builder,
    url: String,
): ImageRequest.Builder {
    val headers = ExternalImageRequestHeaders.forUrl(url)
    if (headers.isEmpty()) return builder
    return builder
        .httpHeaders(headers.toNetworkHeaders())
        .diskCacheKey(url)
}

internal fun String.normalizeExternalImageUrl(): String = fullUrl(Constants.baseUrl)

internal fun shouldProbeImgurPageUrl(url: String): Boolean {
    val parsed = parseUrl(url) ?: return false
    if (parsed.host.lowercase() !in imgurPageHosts) return false
    val segments = parsed.segments.filter { it.isNotBlank() }
    if (segments.size != 1) return false
    return parsed.lastPathSegmentExtension() == null
}

internal fun isDirectImgurImageUrl(url: String): Boolean {
    val parsed = parseUrl(url) ?: return false
    if (parsed.host.lowercase() !in imgurDirectHosts) return false
    return parsed.lastPathSegmentExtension() in supportedImgurImageExtensions
}

internal fun isSupportedImgurImageUrl(url: String): Boolean {
    val parsed = parseUrl(url) ?: return false
    if (parsed.host.lowercase() !in imgurDirectHosts) return false
    return parsed.lastPathSegmentExtension() in supportedImgurImageExtensions
}

internal fun isUnsupportedImgurMediaUrl(url: String): Boolean {
    val parsed = parseUrl(url) ?: return false
    if (parsed.host.lowercase() !in imgurDirectHosts) return false
    return parsed.lastPathSegmentExtension() in unsupportedImgurMediaExtensions
}

internal fun String.isImgurHost(): Boolean = lowercase() in imgurHosts

internal fun Map<String, String>.toNetworkHeaders(): NetworkHeaders =
    NetworkHeaders.Builder().apply {
        forEach { (key, value) -> set(key, value) }
    }.build()

private fun parseUrl(url: String): Url? = runCatching { Url(url) }.getOrNull()

private fun Url.lastPathSegmentExtension(): String? {
    val lastSegment = segments.lastOrNull { it.isNotBlank() } ?: return null
    val extension = lastSegment.substringAfterLast('.', "")
    return extension.takeIf { it.isNotBlank() }?.lowercase()
}
