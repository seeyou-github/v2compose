package io.github.v2compose.di

import android.os.Build
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import coil3.util.DebugLogger
import io.github.v2compose.BuildConfig
import io.github.v2compose.core.CheckInWorker
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.UriDecoder
import io.github.v2compose.core.analytics.IAnalytics
import io.github.v2compose.core.analytics.VendorAnalytics
import io.github.v2compose.datasource.createAccountDataStore
import io.github.v2compose.datasource.createAppDataStore
import io.github.v2compose.network.CookieManager
import io.github.v2compose.network.OkHttpFactory
import io.github.v2compose.network.WebkitCookieManager
import io.github.v2compose.network.createAndroidGithubHttpClient
import io.github.v2compose.network.createAndroidV2HttpClient
import io.github.v2compose.network.di.V2ProxySelector
import io.github.v2compose.shared.core.V2EventManager
import io.github.v2compose.ui.gallery.GalleryViewModel
import io.github.v2compose.ui.login.LoginViewModel
import io.github.v2compose.ui.login.google.GoogleLoginViewModel
import io.github.v2compose.ui.login.twostep.TwoStepLoginViewModel
import io.github.v2compose.ui.main.MainViewModel
import io.github.v2compose.ui.main.home.HomeViewModel
import io.github.v2compose.ui.main.home.recent.RecentViewModel
import io.github.v2compose.ui.main.home.tab.NewsViewModel
import io.github.v2compose.ui.main.mine.MineViewModel
import io.github.v2compose.ui.main.mine.following.MyFollowingViewModel
import io.github.v2compose.ui.main.mine.nodes.MyNodesViewModel
import io.github.v2compose.ui.main.mine.topics.MyTopicsViewModel
import io.github.v2compose.ui.main.nodes.NodesViewModel
import io.github.v2compose.ui.main.notifications.NotificationViewModel
import io.github.v2compose.ui.node.NodeViewModel
import io.github.v2compose.ui.search.SearchViewModel
import io.github.v2compose.ui.settings.SettingsScreenState
import io.github.v2compose.ui.settings.SettingsViewModel
import io.github.v2compose.ui.supplement.AddSupplementViewModel
import io.github.v2compose.ui.topic.TopicViewModel
import io.github.v2compose.ui.user.UserViewModel
import io.github.v2compose.ui.write.WriteTopicViewModel
import io.github.v2compose.usecase.FixHtmlUseCase
import io.ktor.client.HttpClient
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val appModule = module {
    single(named("Account")) { createAccountDataStore(get()) }
    single(named("App")) { createAppDataStore(get()) }

    // Core/App
    single<ExecutorService> { Executors.newFixedThreadPool(4) }
    single<DiskCache> {
        val dir = File(get<android.content.Context>().cacheDir, "image_cache")
        DiskCache.Builder().directory(dir).maxSizePercent(0.02).build()
    }
    single<ImageLoader> {
        ImageLoader.Builder(get<android.content.Context>())
            .components {
                add(KtorNetworkFetcherFactory(get<HttpClient>(named("ImageHttpClient"))))
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .diskCache(get<DiskCache>())
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
    single<ExecutorService> { Executors.newFixedThreadPool(4) }

    singleOf(::UriDecoder)
    single<StringDecoder> { get<UriDecoder>() }

    singleOf(::VendorAnalytics)
    single<IAnalytics> { get<VendorAnalytics>() }
}
val networkModule = module {
    single<io.github.fruit.Fruit> { OkHttpFactory.createFruit() }

    single { WebkitCookieManager() }
    single<okhttp3.CookieJar> { get<WebkitCookieManager>() }
    single<CookieManager> { get<WebkitCookieManager>() }

    singleOf(::V2ProxySelector)
    single<okhttp3.Cache> { OkHttpFactory.createCache(get<android.content.Context>()) }

    single<OkHttpClient>(named("CommonOkHttpClient")) {
        OkHttpFactory.createHttpClient(
            get<okhttp3.CookieJar>(),
            get<okhttp3.Cache>(),
            get<V2ProxySelector>(),
            get<V2EventManager>()
        )
    }
    single<OkHttpClient>(named("ImageOkHttpClient")) {
        OkHttpFactory.createImageHttpClient(get<okhttp3.CookieJar>(), get<V2ProxySelector>())
    }

    single<HttpClient>(named("V2HttpClient")) {
        createAndroidV2HttpClient(
            okHttpClient = get<OkHttpClient>(named("CommonOkHttpClient")),
            fruit = get<io.github.fruit.Fruit>()
        )
    }

    single<HttpClient>(named("ImageHttpClient")) {
        createAndroidV2HttpClient(
            okHttpClient = get<OkHttpClient>(named("ImageOkHttpClient")),
            fruit = get<io.github.fruit.Fruit>()
        )
    }

    single<HttpClient>(named("GithubHttpClient")) {
        createAndroidGithubHttpClient(okHttpClient = get<OkHttpClient>(named("CommonOkHttpClient")))
    }
}

val androidUseCaseModule = module {
    singleOf(::FixHtmlUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::TwoStepLoginViewModel)
    viewModelOf(::GoogleLoginViewModel)
    viewModelOf(::NotificationViewModel)
    viewModelOf(::NodeViewModel)
    viewModelOf(::AddSupplementViewModel)
    viewModelOf(::MyTopicsViewModel)
    viewModelOf(::MineViewModel)
    viewModelOf(::MyFollowingViewModel)
    viewModelOf(::MyNodesViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::NodesViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::RecentViewModel)
    viewModelOf(::TopicViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::WriteTopicViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::NewsViewModel)

    singleOf(::SettingsScreenState)
}

val workerModule = module {
    workerOf(::CheckInWorker)
}

val allModules = listOf(
    appModule, networkModule, androidUseCaseModule, viewModelModule, workerModule
)
