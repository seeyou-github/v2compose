package io.github.v2compose.network

import io.github.fruit.Fruit
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient

actual fun createHttpClientEngine(): HttpClientEngine {
    return OkHttp.create()
}

fun createAndroidV2Client(okHttpClient: OkHttpClient, fruit: Fruit = Fruit.createDefault()): V2Client {
    val engine = OkHttp.create {
        preconfigured = okHttpClient
    }
    return V2Client(engine, fruit)
}
