package io.github.v2compose.network

import android.content.Context
import io.github.v2compose.BuildConfig
import io.github.v2compose.Constants
import io.github.v2compose.shared.bean.RedirectEvent
import io.github.v2compose.shared.core.V2EventManager
import io.github.v2compose.network.NetConstants.keyUserAgent
import io.github.v2compose.network.NetConstants.wapUserAgent
import io.github.v2compose.network.di.V2ProxySelector
import io.github.v2compose.util.Check
import io.github.v2compose.util.L
import io.github.fruit.Fruit
import io.github.fruit.registerGeneratedAdapters
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import java.io.File
import java.util.concurrent.TimeUnit

object OkHttpFactory {

    private const val TIMEOUT_SECONDS: Long = 10

    fun createFruit(): Fruit {
        return Fruit().apply {
            registerGeneratedAdapters()
        }
    }

    fun createHttpClient(
        cookieJar: CookieJar,
        cache: Cache,
        proxySelector: V2ProxySelector,
        eventManager: V2EventManager,
    ): OkHttpClient {
        val builder: OkHttpClient.Builder =
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cache(cache)
                .cookieJar(cookieJar)
                .retryOnConnectionFailure(true)
                .addInterceptor(ConfigInterceptor())
                .addInterceptor(RedirectInterceptor(eventManager))
                .followRedirects(false)
                .followSslRedirects(false)
                .proxySelector(proxySelector)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { msg: String -> L.v(msg) }
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        return builder.build()
    }

    fun createImageHttpClient(cookieJar: CookieJar, proxySelector: V2ProxySelector): OkHttpClient {
        val builder: OkHttpClient.Builder =
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .retryOnConnectionFailure(true)
                .addInterceptor(ConfigInterceptor())
                .followRedirects(true)
                .followSslRedirects(true)
                .proxySelector(proxySelector)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { msg: String -> L.v(msg) }
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        return builder.build()
    }

    fun createCache(context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cacheMaxSize: Long = 100 * 1024 * 1024 //100M
        return Cache(cacheDir, cacheMaxSize)
    }

    private class ConfigInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request: Request = chain.request()
            val ua = request.header(keyUserAgent)
            if (Check.isEmpty(ua)) {
                request = request.newBuilder().addHeader(keyUserAgent, wapUserAgent).build()
            }
            return chain.proceed(request)
        }
    }

    private class RedirectInterceptor(private val eventManager: V2EventManager) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val resp = chain.proceed(chain.request())
            if (resp.isRedirect && chain.request().url.host.contains(Constants.host)) {
                resp.header("location")?.let { eventManager.tryPost(RedirectEvent(it)) }
            }
            return resp
        }
    }


}