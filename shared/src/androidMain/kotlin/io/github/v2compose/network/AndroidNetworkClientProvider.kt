package io.github.v2compose.network

import io.ktor.client.HttpClient

class AndroidNetworkClientProvider(
    private val v2HttpClient: HttpClient,
    private val imageHttpClient: HttpClient,
    private val githubHttpClient: HttpClient,
) : NetworkClientProvider {
    override fun v2HttpClient(): HttpClient = v2HttpClient

    override fun imageHttpClient(): HttpClient = imageHttpClient

    override fun githubHttpClient(): HttpClient = githubHttpClient
}
