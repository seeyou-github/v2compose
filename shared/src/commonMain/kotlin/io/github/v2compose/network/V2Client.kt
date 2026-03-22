package io.github.v2compose.network

import io.github.fruit.Fruit
import io.github.fruit.ktor.fruit
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1")
        }
        
        // 日志 (可选)
        // install(Logging) { ... }
    }
}
