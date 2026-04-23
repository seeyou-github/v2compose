package io.github.v2compose.usecase

import androidx.paging.PagingData
import io.github.v2compose.network.bean.DailyInfo
import io.github.v2compose.network.bean.HomePageInfo
import io.github.v2compose.network.bean.LoginParam
import io.github.v2compose.network.bean.MyFollowingInfo
import io.github.v2compose.network.bean.MyNodesInfo
import io.github.v2compose.network.bean.MyTopicsInfo
import io.github.v2compose.network.bean.NotificationInfo
import io.github.v2compose.network.bean.TwoStepLoginInfo
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.Account
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckInUseCaseTest {

    @Test
    fun treatsMissionDailyRedirectAsSuccessfulCheckIn() = runTest {
        val repository = FakeAccountRepository(
            dailyInfoResponses = ArrayDeque(
                listOf(
                    uncheckedDailyInfo(once = "12027"),
                    checkedInDailyInfo(days = 42),
                ),
            ),
            checkInResult = suspend {
                throw redirectResponseException("/mission/daily")
            },
        )

        val result = CheckInUseCase(repository).invoke()

        assertTrue(result.success)
        assertEquals("已连续登录 42 天", result.message)
        assertEquals(2, repository.dailyInfoCalls)
        assertEquals(listOf("12027"), repository.checkInRequests)
    }

    @Test
    fun returnsFailureForNonDailyRedirect() = runTest {
        val repository = FakeAccountRepository(
            dailyInfoResponses = ArrayDeque(listOf(uncheckedDailyInfo(once = "12027"))),
            checkInResult = suspend {
                throw redirectResponseException("/balance")
            },
        )

        val result = CheckInUseCase(repository).invoke()

        assertFalse(result.success)
        assertEquals(1, repository.dailyInfoCalls)
        assertEquals(listOf("12027"), repository.checkInRequests)
    }

    @Test
    fun returnsFailureForOrdinaryException() = runTest {
        val repository = FakeAccountRepository(
            dailyInfoResponses = ArrayDeque(listOf(uncheckedDailyInfo(once = "12027"))),
            checkInResult = suspend {
                throw IllegalStateException("boom")
            },
        )

        val result = CheckInUseCase(repository).invoke()

        assertFalse(result.success)
        assertEquals("boom", result.message)
        assertEquals(1, repository.dailyInfoCalls)
        assertEquals(listOf("12027"), repository.checkInRequests)
    }

    @Test
    fun returnsFailureWhenMissionDailyFollowUpStillLooksUnchecked() = runTest {
        val repository = FakeAccountRepository(
            dailyInfoResponses = ArrayDeque(
                listOf(
                    uncheckedDailyInfo(once = "12027"),
                    uncheckedDailyInfo(once = "12028"),
                ),
            ),
            checkInResult = suspend {
                throw redirectResponseException("/mission/daily")
            },
        )

        val result = CheckInUseCase(repository).invoke()

        assertFalse(result.success)
        assertNull(result.message)
        assertEquals(2, repository.dailyInfoCalls)
        assertEquals(listOf("12027"), repository.checkInRequests)
    }
}

private class FakeAccountRepository(
    val dailyInfoResponses: ArrayDeque<DailyInfo>,
    private val checkInResult: suspend () -> DailyInfo,
) : AccountRepository {
    var dailyInfoCalls: Int = 0
        private set
    val checkInRequests = mutableListOf<String>()

    override val account: Flow<Account> = flowOf(Account.Empty)
    override val isLoggedIn: Flow<Boolean> = flowOf(true)
    override val unreadNotifications: Flow<Int> = flowOf(0)
    override val hasCheckingInTips: Flow<Boolean> = flowOf(false)
    override val autoCheckIn: Flow<Boolean> = flowOf(false)
    override val lastCheckInTime: Flow<Long> = flowOf(0L)
    override val myTopics: Flow<PagingData<MyTopicsInfo.Item>> = flowOf(PagingData.empty())
    override val myFollowing: Flow<PagingData<MyFollowingInfo.Item>> = flowOf(PagingData.empty())

    override fun getNotifications(): Flow<PagingData<NotificationInfo.Reply>> = flowOf(PagingData.empty())

    override suspend fun resetNotificationCount() = Unit

    override suspend fun getLoginParam(): LoginParam = error("unused")

    override suspend fun login(loginParams: Map<String, String>): LoginParam = error("unused")

    override suspend fun getTwoStepLoginInfo(): TwoStepLoginInfo = error("unused")

    override suspend fun loginNextStep(once: String, code: String): TwoStepLoginInfo = error("unused")

    override suspend fun logout(): Boolean = error("unused")

    override suspend fun getHomePageInfo(): HomePageInfo = error("unused")

    override suspend fun fetchUserInfo() = Unit

    override suspend fun refreshAccount() = Unit

    override suspend fun dailyInfo(): DailyInfo {
        dailyInfoCalls += 1
        return dailyInfoResponses.removeFirstOrNull() ?: error("No dailyInfo response configured")
    }

    override suspend fun checkIn(once: String): DailyInfo {
        checkInRequests += once
        return checkInResult()
    }

    override suspend fun getMyNodes(): MyNodesInfo = error("unused")
}

private fun uncheckedDailyInfo(once: String): DailyInfo = DailyInfo(
    userLink = "/member/alice",
    avatar = "https://cdn.v2ex.com/avatar.png",
    title = "每日登录奖励",
    continuousLoginDaysText = "",
    checkInUrl = "location.href = '/mission/daily/redeem?once=$once';",
)

private fun checkedInDailyInfo(days: Int): DailyInfo = DailyInfo(
    userLink = "/member/alice",
    avatar = "https://cdn.v2ex.com/avatar.png",
    title = "每日登录奖励",
    continuousLoginDaysText = "已连续登录 $days 天",
    checkInUrl = "location.href = '/balance';",
)

private suspend fun redirectResponseException(location: String): ResponseException {
    val client = HttpClient(
        MockEngine {
            respond(
                content = "",
                status = HttpStatusCode.Found,
                headers = headersOf(HttpHeaders.Location, location),
            )
        },
    ) {
        expectSuccess = true
        followRedirects = false
    }

    return try {
        client.get("https://www.v2ex.com/test")
        error("Expected redirect exception")
    } catch (e: ResponseException) {
        e
    } finally {
        client.close()
    }
}
