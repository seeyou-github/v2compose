package io.github.v2compose.network

import io.ktor.client.HttpClient

interface NetworkClientProvider {
    fun v2HttpClient(): HttpClient

    fun imageHttpClient(): HttpClient
}
