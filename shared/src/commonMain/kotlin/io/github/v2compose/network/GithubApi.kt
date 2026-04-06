package io.github.v2compose.network

import io.github.v2compose.network.bean.Release

interface GithubApi {
    suspend fun getTheLatestRelease(owner: String, repo: String): Release
}
