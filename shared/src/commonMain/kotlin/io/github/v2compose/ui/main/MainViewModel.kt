package io.github.v2compose.ui.main

import androidx.compose.runtime.MutableIntState
import io.github.v2compose.PlatformCapabilities
import androidx.lifecycle.viewModelScope
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.network.bean.Release
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.ui.BaseViewModel
import io.github.v2compose.usecase.CheckInUseCase
import io.github.v2compose.usecase.LoadNodesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.daily_mission_failure

class MainViewModel(
    private val checkIn: CheckInUseCase,
    private val appPreferences: AppPreferences,
    private val accountRepository: AccountRepository,
    private val platformCapabilities: PlatformCapabilities,
    private val autoCheckInScheduler: AutoCheckInScheduler,
    private val webViewProxyController: WebViewProxyController,
    val loadNodes: LoadNodesUseCase,
) : BaseViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    val unreadNotifications = accountRepository.unreadNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0,
        )

    init {
        listenCanCheckIn()
        listenAutoCheckIn()
        initWebViewProxy()
    }

    private fun listenCanCheckIn() {
        if (!platformCapabilities.supportsAutoCheckIn) return
        viewModelScope.launch {
            accountRepository.hasCheckingInTips
                .combine(accountRepository.autoCheckIn) { hasCheckingInTips, autoCheckIn ->
                    hasCheckingInTips && autoCheckIn
                }
                .distinctUntilChanged()
                .collectLatest { shouldCheckIn ->
                    if (shouldCheckIn) {
                        checkInInternal()
                    }
                }
        }
    }

    private suspend fun checkInInternal() {
        val result = checkIn()
        if (result.success) {
            result.message?.let { message ->
                updateSnackbarMessage(message)
            }
        } else {
            updateSnackbarMessage(result.message ?: getString(Res.string.daily_mission_failure))
        }
    }

    private fun listenAutoCheckIn() {
        if (!platformCapabilities.supportsAutoCheckIn) return
        viewModelScope.launch {
            accountRepository.isLoggedIn
                .combine(accountRepository.autoCheckIn) { isLoggedIn, autoCheckIn ->
                    isLoggedIn && autoCheckIn
                }
                .distinctUntilChanged()
                .collectLatest { shouldCheckIn ->
                    if (shouldCheckIn) {
                        checkInInternal()
                    }
                    autoCheckInScheduler.syncAutoCheckIn(shouldCheckIn)
                }
        }
    }

    private fun initWebViewProxy() {
        viewModelScope.launch {
            appPreferences.proxyInfo.collectLatest { proxyInfo ->
                if (proxyInfo != ProxyInfo.Default) {
                    webViewProxyController.updateWebViewProxy(proxyInfo)
                }
            }
        }
    }

    fun ignoreRelease(release: Release) {
        viewModelScope.launch {
            appPreferences.ignoredReleaseName(release.tagName)
        }
    }

    fun loadNodes() {
        viewModelScope.launch {
            loadNodes.execute()
        }
    }

    fun updateSelectedTabIndex(index: Int) {
        viewModelScope.launch {
            _selectedTabIndex.emit(index)
        }
    }
}
