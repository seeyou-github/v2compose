package io.github.v2compose.di

import io.github.v2compose.network.NetworkClientProvider
import io.github.v2compose.usecase.ExternalImageUrlResolver
import io.github.v2compose.usecase.HtmlImageLoader
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinResolutionTest {

    @Test
    fun resolvesImagePipelineDependenciesFromKoin() {
        stopKoin()
        initKoin()

        val koin = KoinPlatformTools.defaultContext().get()
        assertNotNull(koin.getOrNull<NetworkClientProvider>())
        assertNotNull(koin.getOrNull<ExternalImageUrlResolver>())
        assertNotNull(koin.getOrNull<HtmlImageLoader>())

        stopKoin()
    }
}
