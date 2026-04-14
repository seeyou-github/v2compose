package io.github.v2compose.usecase

import io.github.fruit.Fruit
import io.github.v2compose.datasource.AccountPreferences
import io.github.v2compose.network.bean.LoginResultInfo
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.util.KLogger
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.first

class UpdateAccountUseCase(
    private val fruit: Fruit,
    private val accountPreferences: AccountPreferences,
    private val accountRepository: AccountRepository,
) {

    companion object {
        private const val TAG: String = "UpdateAccountUseCase"
    }

    suspend fun updateWithNewsInfo(newsInfo: NewsInfo) {
        if (!accountRepository.isLoggedIn.first()) {
            return
        }
        val loginResultInfo: LoginResultInfo? =
            fruit.fromHtml(newsInfo.rawResponse, LoginResultInfo::class)
        if (loginResultInfo == null || !loginResultInfo.isValid()) {
            return
        }
        accountPreferences.updateAccountValues(
            userName = loginResultInfo.userName,
            userAvatar = loginResultInfo.avatar,
        )
    }

    suspend fun updateWithException(e: Exception, userName: String) {
        if (e !is ResponseException) return
        val resp = e.response
        if (resp.status.value !in 300..399) return
        val location = resp.headers[HttpHeaders.Location] ?: return
        val uri = try {
            Url(location)
        } catch (t: Throwable) {
            null
        } ?: return
        if (uri.encodedPath == "/") {
            KLogger.d(
                TAG,
                "updateWithException, location = $location, userName=$userName"
            )
            accountPreferences.updateAccountValues(userName = userName)
        }
    }

}
