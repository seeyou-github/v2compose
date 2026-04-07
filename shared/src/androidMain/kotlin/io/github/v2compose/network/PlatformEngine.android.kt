package io.github.v2compose.network

import io.github.fruit.Fruit
import io.ktor.client.HttpClient
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient

actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()

fun createAndroidV2HttpClient(
    okHttpClient: OkHttpClient,
    fruit: Fruit = Fruit.createDefault()
): HttpClient {
    val engine = OkHttp.create {
        preconfigured = okHttpClient
    }
    return createV2HttpClient(engine, fruit)
}

fun createAndroidGithubHttpClient(okHttpClient: OkHttpClient): HttpClient {
    val engine = OkHttp.create {
        preconfigured = okHttpClient
    }
    return createGithubHttpClient(engine)
}