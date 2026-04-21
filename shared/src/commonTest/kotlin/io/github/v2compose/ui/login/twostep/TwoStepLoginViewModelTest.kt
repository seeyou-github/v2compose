package io.github.v2compose.ui.login.twostep

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import io.github.fruit.Fruit
import io.github.v2compose.datasource.AccountPreferences
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
import io.github.v2compose.usecase.UpdateAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class TwoStepLoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun usesRouteOnceWithoutFetchingTwoStepPage() = runTest(dispatcher) {
        val accountRepository = RecordingAccountRepository()
        val updateAccount = UpdateAccountUseCase(
            fruit = Fruit.createDefault(),
            accountPreferences = AccountPreferences(FakePreferencesDataStore()),
            accountRepository = accountRepository,
        )

        val viewModel = TwoStepLoginViewModel(
            savedStateHandle = SavedStateHandle(mapOf(twoStepArgsOnce to "route-once")),
            accountRepository = accountRepository,
            updateAccount = updateAccount,
        )

        advanceUntilIdle()

        assertEquals(0, accountRepository.twoStepInfoCalls)
        val uiState = assertIs<TwoStepLoginUiState.Success>(viewModel.twoStepLoginUiState.value)
        assertEquals("route-once", uiState.twoStepLoginInfo.once)
    }
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

private class RecordingAccountRepository : AccountRepository {
    var twoStepInfoCalls = 0
        private set

    override val account: Flow<Account> = flowOf(Account.Empty)
    override val isLoggedIn: Flow<Boolean> = flowOf(false)
    override val unreadNotifications: Flow<Int> = flowOf(0)

    override fun getNotifications(): Flow<PagingData<NotificationInfo.Reply>> =
        flowOf(PagingData.empty())

    override suspend fun resetNotificationCount() = Unit

    override suspend fun getLoginParam(): LoginParam = error("unused")

    override suspend fun login(loginParams: Map<String, String>): LoginParam = error("unused")

    override suspend fun getTwoStepLoginInfo(): TwoStepLoginInfo {
        twoStepInfoCalls += 1
        return TwoStepLoginInfo(
            avatar = "avatar",
            title = "两步验证",
            once = "from-repository",
        )
    }

    override suspend fun loginNextStep(once: String, code: String): TwoStepLoginInfo = error("unused")

    override suspend fun logout(): Boolean = true

    override suspend fun getHomePageInfo(): HomePageInfo = error("unused")

    override suspend fun fetchUserInfo() = Unit

    override suspend fun refreshAccount() = Unit

    override val hasCheckingInTips: Flow<Boolean> = flowOf(false)
    override val autoCheckIn: Flow<Boolean> = flowOf(false)
    override val lastCheckInTime: Flow<Long> = flowOf(0L)

    override suspend fun dailyInfo(): DailyInfo = error("unused")

    override suspend fun checkIn(once: String): DailyInfo = error("unused")

    override val myTopics: Flow<PagingData<MyTopicsInfo.Item>> = flowOf(PagingData.empty())
    override val myFollowing: Flow<PagingData<MyFollowingInfo.Item>> = flowOf(PagingData.empty())

    override suspend fun getMyNodes(): MyNodesInfo = error("unused")
}
