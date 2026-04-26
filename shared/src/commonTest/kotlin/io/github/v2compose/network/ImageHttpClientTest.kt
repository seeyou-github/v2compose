package io.github.v2compose.network

import io.github.fruit.Fruit
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ImageHttpClientTest {

    @Test
    fun imageHttpClientSendsImageAcceptHeaderByDefault() = runTest {
        var capturedAccept: String? = null
        var capturedUserAgent: String? = null
        val engine = MockEngine { request ->
            capturedAccept = request.headers[HttpHeaders.Accept]
            capturedUserAgent = request.headers[HttpHeaders.UserAgent]
            respond(
                content = ByteArray(0),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
            )
        }

        val client = createImageHttpClient(
            engine = engine,
        )

        val response = client.get("https://example.com/image.png")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(DEFAULT_IMAGE_ACCEPT_HEADER, capturedAccept)
        assertEquals(NetConstants.wapUserAgent, capturedUserAgent)
    }

    @Test
    fun imageHttpClientDoesNotInstallContentNegotiation() {
        val client = createImageHttpClient(
            engine = MockEngine {
                respond(
                    content = ByteArray(0),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
                )
            },
        )

        assertFalse(client.pluginOrNull(ContentNegotiation) != null)
    }

    @Test
    fun v2HttpClientKeepsJsonOrientedDefaults() = runTest {
        var capturedAccept: String? = null
        var capturedUserAgent: String? = null
        val engine = MockEngine { request ->
            capturedAccept = request.headers[HttpHeaders.Accept]
            capturedUserAgent = request.headers[HttpHeaders.UserAgent]
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val client = createV2HttpClient(
            engine = engine,
            fruit = Fruit.createDefault(),
        )

        val response = client.get("https://example.com/api")
        assertNotNull(response)
        assertEquals("application/json", capturedAccept)
        assertEquals(NetConstants.wapUserAgent, capturedUserAgent)
    }

    @Test
    fun explicitRequestAcceptHeaderOverridesImageDefaults() = runTest {
        var capturedAccept: String? = null
        val engine = MockEngine { request ->
            capturedAccept = request.headers[HttpHeaders.Accept]
            respond(
                content = ByteArray(0),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.JPEG.toString()),
            )
        }

        val client = createImageHttpClient(
            engine = engine,
        )

        client.get("https://example.com/image.jpg") {
            header(HttpHeaders.Accept, "image/jpeg")
        }

        assertEquals("image/jpeg", capturedAccept)
    }
}
