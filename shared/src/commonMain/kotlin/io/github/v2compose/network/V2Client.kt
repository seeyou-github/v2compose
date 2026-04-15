package io.github.v2compose.network

import io.github.fruit.Fruit
import io.github.fruit.ktor.fruit
import io.github.v2compose.Constants
import io.github.v2compose.isSameAuthFlow
import io.github.v2compose.shared.bean.RedirectEvent
import io.github.v2compose.shared.core.V2EventManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Shared Ktor Client for V2EX
 */
fun createV2HttpClient(
    engine: HttpClientEngine? = null,
    fruit: Fruit = Fruit.createDefault(),
    eventManager: V2EventManager? = null,
): HttpClient = HttpClient(engine ?: createHttpClientEngine()) {
    expectSuccess = true

    install(ContentNegotiation) {
        // 支持 JSON 解析 (针对 V2EX API)
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
        // 支持 HTML 解析 (针对 V2EX 网页)
        fruit(fruit)
    }

    // 默认配置
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "www.v2ex.com"
        }
        if (!headers.contains(NetConstants.keyUserAgent)) {
            header(HttpHeaders.UserAgent, NetConstants.wapUserAgent)
        }
    }

    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, request ->
            handleAuthRedirectException(cause, request, eventManager)
        }
    }

    // 日志 (可选)
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.BODY
        format = LoggingFormat.OkHttp
    }
}

fun createGithubHttpClient(engine: HttpClientEngine? = null): HttpClient =
    HttpClient(engine ?: createHttpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

internal fun resolveAuthRedirectEventLocation(
    requestUrl: String,
    redirectLocation: String?,
): String? {
    if (redirectLocation.isNullOrBlank()) return null
    val requestHost = runCatching { Url(requestUrl).host }.getOrNull() ?: return null
    if (!requestHost.endsWith(Constants.host)) return null
    if (isSameAuthFlow(requestUrl, redirectLocation)) return null
    return redirectLocation
}

private fun handleAuthRedirectException(
    cause: Throwable,
    request: HttpRequest,
    eventManager: V2EventManager?,
) {
    if (eventManager == null || cause !is ResponseException) return
    if (cause.response.status.value !in 300..399) return
    resolveAuthRedirectEventLocation(
        requestUrl = request.url.toString(),
        redirectLocation = cause.response.headers[HttpHeaders.Location],
    )?.let { eventManager.tryPost(RedirectEvent(it)) }
}
