package io.github.v2compose.usecase

import io.github.v2compose.network.NetworkClientProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExternalImageUrlResolverTest {

    @Test
    fun returnsOriginalUrlForNonImgurUrl() = runTest {
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                error("Non-Imgur URL should not trigger probe")
            },
        )

        assertEquals(
            "https://example.com/image.png",
            resolver.resolve("https://example.com/image.png"),
        )
    }

    @Test
    fun resolvesSingleResourceImgurPageToDirectImageUrl() = runTest {
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                ImageProbeResult(
                    finalRequestUrl = "https://i.imgur.com/Ug1iMq4.png",
                    statusCode = 200,
                    redirectLocation = null,
                )
            },
        )

        assertEquals(
            "https://i.imgur.com/Ug1iMq4.png",
            resolver.resolve("https://imgur.com/Ug1iMq4"),
        )
    }

    @Test
    fun probesMobileImgurSingleResourcePageUrl() = runTest {
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                ImageProbeResult(
                    finalRequestUrl = "https://i.imgur.com/Ug1iMq4.png",
                    statusCode = 200,
                    redirectLocation = null,
                )
            },
        )

        assertEquals(
            "https://i.imgur.com/Ug1iMq4.png",
            resolver.resolve("https://m.imgur.com/Ug1iMq4"),
        )
    }

    @Test
    fun doesNotProbeDirectImgurImageUrl() = runTest {
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                error("Direct i.imgur.com image should not trigger probe")
            },
        )

        assertEquals(
            "https://i.imgur.com/Ug1iMq4.png",
            resolver.resolve("https://i.imgur.com/Ug1iMq4.png"),
        )
    }

    @Test
    fun fallsBackToOriginalUrlForUnsupportedMediaType() = runTest {
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                ImageProbeResult(
                    finalRequestUrl = "https://i.imgur.com/Ug1iMq4.mp4",
                    statusCode = 200,
                    redirectLocation = null,
                )
            },
        )

        assertEquals(
            "https://imgur.com/Ug1iMq4",
            resolver.resolve("https://imgur.com/Ug1iMq4"),
        )
    }

    @Test
    fun cachesProbeResultForSameRawUrl() = runTest {
        var probeCount = 0
        val resolver = DefaultExternalImageUrlResolver(
            networkClientProvider = FakeNetworkClientProvider(),
            probe = { _, _ ->
                probeCount += 1
                ImageProbeResult(
                    finalRequestUrl = "https://i.imgur.com/Ug1iMq4.png",
                    statusCode = 200,
                    redirectLocation = null,
                )
            },
        )

        val rawUrl = "https://imgur.com/Ug1iMq4"
        assertEquals("https://i.imgur.com/Ug1iMq4.png", resolver.resolve(rawUrl))
        assertEquals("https://i.imgur.com/Ug1iMq4.png", resolver.resolve(rawUrl))
        assertEquals(1, probeCount)
    }
}

private class FakeNetworkClientProvider : NetworkClientProvider {
    override fun v2HttpClient(): HttpClient = HttpClient()

    override fun imageHttpClient(): HttpClient = HttpClient()
}
