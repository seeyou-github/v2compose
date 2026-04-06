package io.github.v2compose.network

import io.github.fruit.Fruit
import io.github.fruit.ktor.fruit
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LoggingFormat
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Shared Ktor Client for V2EX
 */
class V2Client(
    engine: HttpClientEngine? = null,
    private val fruit: Fruit = Fruit.createDefault()
) {
    val httpClient = HttpClient(engine ?: createHttpClientEngine()) {
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

        // 日志 (可选)
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
            format = LoggingFormat.OkHttp
        }
    }
}
