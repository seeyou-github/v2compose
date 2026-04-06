package io.github.v2compose.di

import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.v2compose.V2AppState
import io.github.v2compose.V2AppViewModel
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.UriDecoder
import io.github.v2compose.core.analytics.IAnalytics
import io.github.v2compose.core.analytics.VendorAnalytics
import io.github.v2compose.datasource.AccountPreferences
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.datasource.AppStateStore
import io.github.v2compose.datasource.MyFollowingPagingSource
import io.github.v2compose.datasource.MyTopicsPagingSource
import io.github.v2compose.datasource.createAccountDataStore
import io.github.v2compose.datasource.createAppDataStore
import io.github.v2compose.network.GithubService
import io.github.v2compose.network.OkHttpFactory
import io.github.v2compose.network.V2exApi
import io.github.v2compose.network.WebkitCookieManager
import io.github.v2compose.network.di.V2ProxySelector
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.repository.AppRepository
import io.github.v2compose.repository.NewsRepository
import io.github.v2compose.repository.NodeRepository
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.repository.UserRepository
import io.github.v2compose.repository.def.DefaultAccountRepository
import io.github.v2compose.repository.def.DefaultAppRepository
import io.github.v2compose.repository.def.DefaultNewsRepository
import io.github.v2compose.repository.def.DefaultNodeRepository
import io.github.v2compose.repository.def.DefaultTopicRepository
import io.github.v2compose.repository.def.DefaultUserRepository
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
import io.github.v2compose.usecase.CheckForUpdatesUseCase
import io.github.v2compose.usecase.CheckInUseCase
import io.github.v2compose.usecase.FixHtmlUseCase
import io.github.v2compose.usecase.LoadNodesUseCase
import io.github.v2compose.usecase.UpdateAccountUseCase
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val appModule = module {
    single(named("Account")) { createAccountDataStore(get()) }
    single(named("App")) { createAppDataStore(get()) }

    // Core/App
    single<Moshi> { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
    single<DiskCache> {
        val dir = File(get<android.content.Context>().cacheDir, "image_cache")
        DiskCache.Builder().directory(dir).maxSizePercent(0.02).build()
    }
    single<ImageLoader> {
        ImageLoader.Builder(get<android.content.Context>())
            .okHttpClient(get<OkHttpClient>(named("ImageOkHttpClient")))
            .diskCache(get<DiskCache>())
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }.build()
    }
    single<ExecutorService> { Executors.newFixedThreadPool(4) }

    singleOf(::UriDecoder)
    single<StringDecoder> { get<UriDecoder>() }

    singleOf(::VendorAnalytics)
    single<IAnalytics> { get<VendorAnalytics>() }
}

val networkModule = module {
    single<com.google.gson.Gson> { OkHttpFactory.createGson() }
    single<io.github.fruit.Fruit> { OkHttpFactory.createFruit() }
    single { OkHttpFactory.createCookieManager() }
    single<okhttp3.CookieJar> { get<WebkitCookieManager>() }
    singleOf(::V2ProxySelector)

    single<OkHttpClient>(named("CommonOkHttpClient")) {
        OkHttpFactory.createHttpClient(
            get<okhttp3.CookieJar>(),
            get<okhttp3.Cache>(),
            get<V2ProxySelector>()
        )
    }
    single<OkHttpClient>(named("ImageOkHttpClient")) {
        OkHttpFactory.createImageHttpClient(get<okhttp3.CookieJar>(), get<V2ProxySelector>())
    }

    single<okhttp3.Cache> { OkHttpFactory.createCache(get<android.content.Context>()) }

    single<V2exApi> {
        val client = io.github.v2compose.network.createAndroidV2Client(
            okHttpClient = get<OkHttpClient>(named("CommonOkHttpClient")),
            fruit = get<io.github.fruit.Fruit>()
        )
        V2exApi(client.httpClient)
    }

    single<GithubService> {
        GithubService.createGithubApi(
            get<OkHttpClient>(named("CommonOkHttpClient")),
            get<com.google.gson.Gson>()
        )
    }
}

val dataModule = module {
    singleOf(::DefaultAppRepository) { bind<AppRepository>() }
    singleOf(::DefaultNewsRepository) { bind<NewsRepository>() }
    singleOf(::DefaultNodeRepository) { bind<NodeRepository>() }
    singleOf(::DefaultTopicRepository) { bind<TopicRepository>() }
    singleOf(::DefaultUserRepository) { bind<UserRepository>() }
    singleOf(::DefaultAccountRepository) { bind<AccountRepository>() }

    // DataSources - DataStore instances are created in appModule
    single { AppPreferences(get(named("App"))) }
    single { AccountPreferences(get(named("Account"))) }
    singleOf(::AppStateStore)
}

val useCaseModule = module {
    singleOf(::CheckForUpdatesUseCase)
    singleOf(::FixHtmlUseCase)
    singleOf(::UpdateAccountUseCase)
    singleOf(::CheckInUseCase)
    singleOf(::LoadNodesUseCase)
}

val viewModelModule = module {
    viewModelOf(::V2AppViewModel)
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

    // States that injected
    singleOf(::V2AppState)
    singleOf(::SettingsScreenState)
}

val pagingModule = module {
    factoryOf(::MyTopicsPagingSource)
    factoryOf(::MyFollowingPagingSource)
}

val allModules = listOf(
    appModule, networkModule, dataModule, useCaseModule, viewModelModule, pagingModule
)
