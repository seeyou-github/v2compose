package io.github.v2compose.network

import io.github.v2compose.network.bean.Release
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface GithubApi {
    suspend fun getTheLatestRelease(owner: String, repo: String): Release
}

class KtorGithubApi(private val client: HttpClient) : GithubApi {
    override suspend fun getTheLatestRelease(owner: String, repo: String): Release =
        client.get("https://api.github.com/repos/$owner/$repo/releases/latest").body()
}
