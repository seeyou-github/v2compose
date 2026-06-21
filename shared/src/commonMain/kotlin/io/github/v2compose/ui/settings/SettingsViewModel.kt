package io.github.v2compose.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.network.HttpCacheManager
import io.github.v2compose.network.ProxyManager
import io.github.v2compose.network.bean.Release
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.AppSettings
import io.github.v2compose.shared.bean.DarkMode
import io.github.v2compose.shared.bean.ProxyInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val appPreferences: AppPreferences,
    private val accountRepository: AccountRepository,
    private val httpCache: HttpCacheManager,
    private val imageDiskCache: DiskCache,
    private val proxyManager: ProxyManager,
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = appPreferences.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppSettings.Default,
        )

    val proxyInfo: StateFlow<ProxyInfo> = appPreferences.proxyInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ProxyInfo.Default,
        )
    private val _cacheSize = MutableStateFlow(0L)
    val cacheSize = _cacheSize.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = accountRepository.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        false,
    )

    init {
        initCacheSize()
    }

    @OptIn(ExperimentalCoilApi::class)
    private fun initCacheSize() {
        viewModelScope.launch {
            val imageCacheSize = imageDiskCache.size.div(1024 * 1024)
            val httpCacheSize = httpCache.size.div(1024 * 1024)
            _cacheSize.emit(imageCacheSize + httpCacheSize)
        }
    }

    fun updateAutoCheckIn(value: Boolean) {
        viewModelScope.launch {
            appPreferences.autoCheckIn(value)
        }
    }

    fun updateReplyWithFloor(value: Boolean) {
        viewModelScope.launch {
            appPreferences.replyWithFloor(value)
        }
    }

    fun setOpenInInternalBrowser(value: Boolean) {
        viewModelScope.launch {
            appPreferences.openInInternalBrowser(value)
        }
    }

    fun setDarkMode(value: DarkMode) {
        viewModelScope.launch {
            appPreferences.darkMode(value)
        }
    }

    fun setTopicTitleTwoLineMax(value: Boolean) {
        viewModelScope.launch {
            appPreferences.topicTitleOverview(value)
        }
    }

    fun toggleHighlightOpReply(value: Boolean) {
        viewModelScope.launch {
            appPreferences.highlightOpReply(value)
        }
    }

    fun setHideLoginRelatedUi(value: Boolean) {
        viewModelScope.launch {
            appPreferences.hideLoginRelatedUi(value)
        }
    }

    fun setHideTopicUserInfo(value: Boolean) {
        viewModelScope.launch {
            appPreferences.hideTopicUserInfo(value)
        }
    }

    fun changeProxy(proxy: ProxyInfo) {
        viewModelScope.launch {
            appPreferences.proxyInfo(proxy)
            proxyManager.updateProxy(proxy)
        }
    }


    @OptIn(ExperimentalCoilApi::class)
    fun clearCache() {
        viewModelScope.launch {
            imageDiskCache.clear()
            httpCache.clear()
            _cacheSize.emit(0L)
        }
    }

    fun ignoreRelease(release: Release) {
        viewModelScope.launch {
            appPreferences.ignoredReleaseName(release.tagName)
        }
    }

    suspend fun logout() {
        accountRepository.logout()
    }

}
