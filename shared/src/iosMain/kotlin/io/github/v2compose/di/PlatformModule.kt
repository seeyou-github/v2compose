package io.github.v2compose.di

import coil3.ImageLoader
import coil3.PlatformContext as CoilPlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import io.github.fruit.Fruit
import io.github.v2compose.core.PlatformContext as AppPlatformContext
import io.github.v2compose.datasource.createAccountDataStore
import io.github.v2compose.datasource.createAppDataStore
import io.github.v2compose.network.CookieManager
import io.github.v2compose.network.HttpCacheManager
import io.github.v2compose.network.IosCookieManager
import io.github.v2compose.network.NoOpHttpCacheManager
import io.github.v2compose.network.NoOpProxyManager
import io.github.v2compose.network.ProxyManager
import io.github.v2compose.network.createGithubHttpClient
import io.github.v2compose.network.createV2HttpClient
import io.github.v2compose.ui.main.IosMainPlatformDelegate
import io.github.v2compose.ui.main.MainPlatformDelegate
import io.github.v2compose.usecase.FixHtmlUseCase
import io.github.v2compose.usecase.HtmlImageLoader
import io.ktor.client.HttpClient
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module = module {
    single<AppPlatformContext> { IosAppPlatformContext }
    single<CoilPlatformContext> { CoilPlatformContext.INSTANCE }

    single(named("Account")) { createAccountDataStore(get<AppPlatformContext>()) }
    single(named("App")) { createAppDataStore(get<AppPlatformContext>()) }

    single<DiskCache> {
        DiskCache.Builder()
            .directory(iosCacheDirectory("image_cache"))
            .maxSizePercent(0.02)
            .build()
    }
    single<ImageLoader> {
        ImageLoader.Builder(get<CoilPlatformContext>())
            .components {
                add(KtorNetworkFetcherFactory(get<HttpClient>(named("ImageHttpClient"))))
                add(SvgDecoder.Factory())
            }
            .diskCache(get<DiskCache>())
            .build()
    }

    single<MainPlatformDelegate> { IosMainPlatformDelegate() }
    single<Fruit> { Fruit.createDefault() }
    single<CookieManager> { IosCookieManager() }
    single<ProxyManager> { NoOpProxyManager() }
    single<HttpCacheManager> { NoOpHttpCacheManager() }

    single<HttpClient>(named("V2HttpClient")) {
        createV2HttpClient(engine = null, fruit = get<Fruit>())
    }
    single<HttpClient>(named("ImageHttpClient")) {
        createV2HttpClient(engine = null, fruit = get<Fruit>())
    }
    single<HttpClient>(named("GithubHttpClient")) {
        createGithubHttpClient()
    }

    singleOf(::FixHtmlUseCase)
    single<HtmlImageLoader> { get<FixHtmlUseCase>() }
}

private object IosAppPlatformContext : AppPlatformContext()

@OptIn(ExperimentalForeignApi::class)
private fun iosCacheDirectory(child: String) = buildString {
    val cacheDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    requireNotNull(cacheDirectory?.path) { "Failed to resolve iOS cache directory." }
    append(cacheDirectory.path)
    append('/')
    append(child)
}.toPath()
