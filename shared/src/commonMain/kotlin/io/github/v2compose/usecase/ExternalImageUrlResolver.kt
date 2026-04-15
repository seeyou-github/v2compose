package io.github.v2compose.usecase

import io.github.v2compose.network.NetworkClientProvider
import io.github.v2compose.util.KLogger
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "ExternalImageUrlResolver"

fun interface ExternalImageUrlResolver {
    suspend fun resolve(rawUrl: String): String
}

data class ImageProbeResult(
    val finalRequestUrl: String,
    val statusCode: Int,
    val redirectLocation: String?,
)

class DefaultExternalImageUrlResolver(
    private val networkClientProvider: NetworkClientProvider,
) : ExternalImageUrlResolver {
    private var probeOverride: (suspend (rawUrl: String, headers: Map<String, String>) -> ImageProbeResult?)? =
        null

    internal constructor(
        networkClientProvider: NetworkClientProvider,
        probe: suspend (rawUrl: String, headers: Map<String, String>) -> ImageProbeResult?,
    ) : this(networkClientProvider) {
        this.probeOverride = probe
    }

    private val cacheMutex = Mutex()
    private val resolvedUrlCache = mutableMapOf<String, String>()

    override suspend fun resolve(rawUrl: String): String {
        val normalizedUrl = rawUrl.normalizeExternalImageUrl()
        cacheMutex.withLock {
            resolvedUrlCache[normalizedUrl]
        }?.let { return it }

        val resolvedUrl = if (
            shouldProbeImgurPageUrl(normalizedUrl) &&
            !isDirectImgurImageUrl(normalizedUrl)
        ) {
            resolveImgurPageUrl(normalizedUrl)
        } else {
            normalizedUrl
        }

        cacheMutex.withLock {
            resolvedUrlCache[normalizedUrl] = resolvedUrl
        }
        return resolvedUrl
    }

    private suspend fun resolveImgurPageUrl(rawUrl: String): String {
        val headers = ExternalImageRequestHeaders.forUrl(rawUrl, probe = true)
        val result = runCatching { probe(rawUrl, headers) }
            .onFailure { throwable ->
                KLogger.w(TAG, "imgur probe failed: rawUrl=$rawUrl, error=${throwable.message}")
            }
            .getOrNull()

        if (result == null) {
            logFallback(
                rawUrl = rawUrl,
                resolvedUrl = rawUrl,
                finalRequestUrl = null,
                statusCode = null,
                redirectLocation = null,
                fallbackReason = "probe_failed",
            )
            return rawUrl
        }

        if (result.statusCode in 200..299 && isSupportedImgurImageUrl(result.finalRequestUrl)) {
            KLogger.d(
                TAG,
                "imgur probe success: rawUrl=$rawUrl, resolvedUrl=${result.finalRequestUrl}, " +
                    "finalRequestUrl=${result.finalRequestUrl}, status=${result.statusCode}, " +
                    "location=${result.redirectLocation}",
            )
            return result.finalRequestUrl
        }

        val fallbackReason = when {
            isUnsupportedImgurMediaUrl(result.finalRequestUrl) -> "unsupported_media_type"
            result.statusCode in 300..399 -> "redirect_loop_or_redirect_response"
            !isSupportedImgurImageUrl(result.finalRequestUrl) -> "final_url_not_supported_image"
            else -> "probe_no_canonical_image"
        }
        logFallback(
            rawUrl = rawUrl,
            resolvedUrl = rawUrl,
            finalRequestUrl = result.finalRequestUrl,
            statusCode = result.statusCode,
            redirectLocation = result.redirectLocation,
            fallbackReason = fallbackReason,
        )
        return rawUrl
    }

    private fun logFallback(
        rawUrl: String,
        resolvedUrl: String,
        finalRequestUrl: String?,
        statusCode: Int?,
        redirectLocation: String?,
        fallbackReason: String,
    ) {
        KLogger.w(
            TAG,
            "imgur probe fallback: rawUrl=$rawUrl, resolvedUrl=$resolvedUrl, " +
            "finalRequestUrl=$finalRequestUrl, status=$statusCode, " +
                "location=$redirectLocation, fallbackReason=$fallbackReason",
        )
    }
    private suspend fun probe(
        rawUrl: String,
        headers: Map<String, String>,
    ): ImageProbeResult? {
        return probeOverride?.invoke(rawUrl, headers)
            ?: defaultProbe(networkClientProvider, rawUrl, headers)
    }
}

private suspend fun defaultProbe(
    networkClientProvider: NetworkClientProvider,
    rawUrl: String,
    headers: Map<String, String>,
): ImageProbeResult? {
    val response = networkClientProvider.imageHttpClient().get(rawUrl) {
        expectSuccess = false
        headers.forEach { (key, value) ->
            header(key, value)
        }
    }
    return ImageProbeResult(
        finalRequestUrl = response.call.request.url.toString(),
        statusCode = response.status.value,
        redirectLocation = response.headers[HttpHeaders.Location],
    )
}
