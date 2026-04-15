package io.github.v2compose.network

import io.github.fruit.Fruit
import io.github.v2compose.shared.core.V2EventManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient

actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()

fun createAndroidV2HttpClient(
    okHttpClient: OkHttpClient,
    fruit: Fruit = Fruit.createDefault(),
    eventManager: V2EventManager? = null,
): HttpClient {
    val engine = OkHttp.create {
        preconfigured = okHttpClient
    }
    return createV2HttpClient(engine, fruit, eventManager)
}

fun createAndroidGithubHttpClient(okHttpClient: OkHttpClient): HttpClient {
    val engine = OkHttp.create {
        preconfigured = okHttpClient
    }
    return createGithubHttpClient(engine)
}
