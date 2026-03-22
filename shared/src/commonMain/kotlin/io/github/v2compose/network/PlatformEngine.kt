package io.github.v2compose.network

import io.ktor.client.engine.*

expect fun createHttpClientEngine(): HttpClientEngine
