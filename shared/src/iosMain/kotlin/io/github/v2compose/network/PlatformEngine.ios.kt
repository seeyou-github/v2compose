package io.github.v2compose.network

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun createHttpClientEngine(): HttpClientEngine {
    return Darwin.create()
}
