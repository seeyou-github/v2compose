package io.github.v2compose.network

import io.github.v2compose.network.bean.Release
import io.ktor.client.call.body
import io.ktor.client.request.get

interface GithubApi {
    suspend fun getTheLatestRelease(owner: String, repo: String): Release
}

class KtorGithubApi(private val clientProvider: NetworkClientProvider) : GithubApi {
    override suspend fun getTheLatestRelease(owner: String, repo: String): Release =
        clientProvider.githubHttpClient()
            .get("https://api.github.com/repos/$owner/$repo/releases/latest")
            .body()
}
