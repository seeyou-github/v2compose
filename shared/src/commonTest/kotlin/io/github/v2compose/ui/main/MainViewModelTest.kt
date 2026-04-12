package io.github.v2compose.ui.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.paging.PagingData
import io.github.v2compose.PlatformCapabilities
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.network.bean.AppendTopicPageInfo
import io.github.v2compose.network.bean.CreateTopicPageInfo
import io.github.v2compose.network.bean.DailyInfo
import io.github.v2compose.network.bean.HomePageInfo
import io.github.v2compose.network.bean.LoginParam
import io.github.v2compose.network.bean.MyFollowingInfo
import io.github.v2compose.network.bean.MyNodesInfo
import io.github.v2compose.network.bean.MyTopicsInfo
import io.github.v2compose.network.bean.NotificationInfo
import io.github.v2compose.network.bean.ReplyTopicResultInfo
import io.github.v2compose.network.bean.Release
import io.github.v2compose.network.bean.SoV2EXSearchResultInfo
import io.github.v2compose.network.bean.TopicInfo
import io.github.v2compose.network.bean.TwoStepLoginInfo
import io.github.v2compose.network.bean.V2exResult
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.repository.ActionMethod
import io.github.v2compose.repository.AppRepository
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.shared.bean.Account
import io.github.v2compose.shared.bean.ContentFormat
import io.github.v2compose.shared.bean.DraftTopic
import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.shared.bean.ProxyType
import io.github.v2compose.shared.bean.TopicNode
import io.github.v2compose.usecase.CheckForUpdatesUseCase
import io.github.v2compose.usecase.CheckInUseCase
import io.github.v2compose.usecase.LoadNodesUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
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
    fun doesNotStartAutoCheckInWhenPlatformDoesNotSupportIt() = runTest(dispatcher) {
        val accountRepository = FakeAccountRepository(
            isLoggedIn = true,
            autoCheckIn = true,
            hasCheckingInTips = true,
        )
        val autoCheckInScheduler = RecordingAutoCheckInScheduler()
        val webViewProxyController = RecordingWebViewProxyController()

        MainViewModel(
            checkForUpdates = CheckForUpdatesUseCase(FakeAppRepository(), fakeAppPreferences()),
            checkIn = CheckInUseCase(accountRepository),
            appPreferences = fakeAppPreferences(),
            accountRepository = accountRepository,
            platformCapabilities = PlatformCapabilities.Ios,
            autoCheckInScheduler = autoCheckInScheduler,
            webViewProxyController = webViewProxyController,
            loadNodes = LoadNodesUseCase(FakeTopicRepository()),
        )

        advanceUntilIdle()

        assertEquals(0, accountRepository.dailyInfoCalls)
        assertTrue(autoCheckInScheduler.autoCheckInStates.isEmpty())
        assertTrue(webViewProxyController.proxyUpdates.isEmpty())
    }

    @Test
    fun keepsExistingAutoCheckInBehaviorWhenPlatformSupportsIt() = runTest(dispatcher) {
        val accountRepository = FakeAccountRepository(
            isLoggedIn = true,
            autoCheckIn = true,
            hasCheckingInTips = true,
        )
        val autoCheckInScheduler = RecordingAutoCheckInScheduler()
        val webViewProxyController = RecordingWebViewProxyController()

        MainViewModel(
            checkForUpdates = CheckForUpdatesUseCase(FakeAppRepository(), fakeAppPreferences()),
            checkIn = CheckInUseCase(accountRepository),
            appPreferences = fakeAppPreferences(),
            accountRepository = accountRepository,
            platformCapabilities = PlatformCapabilities.Android,
            autoCheckInScheduler = autoCheckInScheduler,
            webViewProxyController = webViewProxyController,
            loadNodes = LoadNodesUseCase(FakeTopicRepository()),
        )

        advanceUntilIdle()

        assertTrue(accountRepository.dailyInfoCalls > 0)
        assertTrue(autoCheckInScheduler.autoCheckInStates.contains(true))
    }

    @Test
    fun updatesWebViewProxyWhenCustomProxyIsConfigured() = runTest(dispatcher) {
        val proxyInfo = ProxyInfo(type = ProxyType.Http, address = "127.0.0.1", port = 7890)
        val appPreferences = fakeAppPreferences()
        val accountRepository = FakeAccountRepository(false, false, false)
        val autoCheckInScheduler = RecordingAutoCheckInScheduler()
        val webViewProxyController = RecordingWebViewProxyController()
        appPreferences.proxyInfo(proxyInfo)

        MainViewModel(
            checkForUpdates = CheckForUpdatesUseCase(FakeAppRepository(), appPreferences),
            checkIn = CheckInUseCase(accountRepository),
            appPreferences = appPreferences,
            accountRepository = accountRepository,
            platformCapabilities = PlatformCapabilities.Android,
            autoCheckInScheduler = autoCheckInScheduler,
            webViewProxyController = webViewProxyController,
            loadNodes = LoadNodesUseCase(FakeTopicRepository()),
        )

        advanceUntilIdle()

        assertEquals(1, webViewProxyController.proxyUpdates.size)
        assertEquals(proxyInfo, webViewProxyController.proxyUpdates.single())
    }
}

private fun fakeAppPreferences(): AppPreferences = AppPreferences(FakePreferencesDataStore())

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

private class RecordingAutoCheckInScheduler : AutoCheckInScheduler {
    val autoCheckInStates = mutableListOf<Boolean>()

    override fun syncAutoCheckIn(enabled: Boolean) {
        autoCheckInStates += enabled
    }
}

private class RecordingWebViewProxyController : WebViewProxyController {
    val proxyUpdates = mutableListOf<ProxyInfo>()

    override fun updateWebViewProxy(proxyInfo: ProxyInfo) {
        proxyUpdates += proxyInfo
    }
}

private class FakeAppRepository : AppRepository {
    override suspend fun getAppLatestRelease(): Release = Release.Empty
}

private class FakeTopicRepository : TopicRepository {
    override suspend fun getTopicInfo(topicId: String): TopicInfo = error("unused")

    override fun getTopic(topicId: String, initialPage: Int?, reversed: Boolean): Flow<PagingData<Any>> =
        flowOf(PagingData.empty())

    override val repliesOrderReversed: Flow<Boolean> = flowOf(true)

    override suspend fun toggleRepliesReversed() = Unit

    override val highlightOpReply: Flow<Boolean> = flowOf(false)

    override fun search(keyword: String): Flow<PagingData<SoV2EXSearchResultInfo.Hit>> =
        flowOf(PagingData.empty())

    override val topicTitleOverview: Flow<Boolean> = flowOf(true)

    override val replyWithFloor: Flow<Boolean> = flowOf(true)

    override suspend fun doTopicAction(
        action: String,
        method: ActionMethod,
        topicId: String,
        once: String,
    ): V2exResult = error("unused")

    override suspend fun doReplyAction(
        action: String,
        method: ActionMethod,
        topicId: String,
        replyId: String,
        once: String,
    ): V2exResult = error("unused")

    override suspend fun ignoreReply(topicId: String, replyId: String, once: String): Boolean =
        error("unused")

    override suspend fun replyTopic(
        topicId: String,
        content: String,
        once: String,
    ): ReplyTopicResultInfo = error("unused")

    override val draftTopic: Flow<DraftTopic> = flowOf(DraftTopic.Empty)

    override suspend fun saveDraftTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        node: TopicNode?,
    ) = Unit

    override suspend fun getCreateTopicPageInfo(): CreateTopicPageInfo = error("unused")

    override suspend fun getTopicNodes(): List<TopicNode> = emptyList()

    override suspend fun createTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        nodeName: String,
        once: String,
    ): CreateTopicPageInfo = error("unused")

    override suspend fun getAppendTopicPageInfo(topicId: String): AppendTopicPageInfo = error("unused")

    override suspend fun addSupplement(
        topicId: String,
        supplement: String,
        contentFormat: ContentFormat,
        once: String,
    ): AppendTopicPageInfo = error("unused")
}

private class FakeAccountRepository(
    isLoggedIn: Boolean,
    autoCheckIn: Boolean,
    hasCheckingInTips: Boolean,
) : AccountRepository {
    private val loggedInFlow = MutableStateFlow(isLoggedIn)
    private val autoCheckInFlow = MutableStateFlow(autoCheckIn)
    private val hasCheckingInTipsFlow = MutableStateFlow(hasCheckingInTips)
    private val lastCheckInTimeFlow = MutableStateFlow(0L)

    var dailyInfoCalls = 0
        private set

    override val account: Flow<Account> = flowOf(Account.Empty)
    override val isLoggedIn: Flow<Boolean> = loggedInFlow
    override val unreadNotifications: Flow<Int> = flowOf(0)

    override fun getNotifications(): Flow<PagingData<NotificationInfo.Reply>> =
        flowOf(PagingData.empty())

    override suspend fun resetNotificationCount() = Unit

    override suspend fun getLoginParam(): LoginParam = error("unused")

    override suspend fun login(loginParams: Map<String, String>): LoginParam = error("unused")

    override suspend fun getTwoStepLoginInfo(): TwoStepLoginInfo = error("unused")

    override suspend fun loginNextStep(once: String, code: String): TwoStepLoginInfo =
        error("unused")

    override suspend fun logout(): Boolean = true

    override suspend fun getHomePageInfo(): HomePageInfo = error("unused")

    override suspend fun fetchUserInfo() = Unit

    override suspend fun refreshAccount() = Unit

    override val hasCheckingInTips: Flow<Boolean> = hasCheckingInTipsFlow
    override val autoCheckIn: Flow<Boolean> = autoCheckInFlow
    override val lastCheckInTime: Flow<Long> = lastCheckInTimeFlow

    override suspend fun dailyInfo(): DailyInfo {
        dailyInfoCalls += 1
        return DailyInfo().apply {
            continuousLoginDaysText = "已连续登录 1 天"
            checkInUrl = "location.href = '/balance';"
        }
    }

    override suspend fun checkIn(once: String): DailyInfo {
        dailyInfoCalls += 1
        return DailyInfo().apply {
            continuousLoginDaysText = "已连续登录 1 天"
            checkInUrl = "location.href = '/balance';"
        }
    }

    override val myTopics: Flow<PagingData<MyTopicsInfo.Item>> = flowOf(PagingData.empty())
    override val myFollowing: Flow<PagingData<MyFollowingInfo.Item>> = flowOf(PagingData.empty())

    override suspend fun getMyNodes(): MyNodesInfo = error("unused")
}
