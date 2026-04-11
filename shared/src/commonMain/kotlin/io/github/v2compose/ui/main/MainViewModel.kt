package io.github.v2compose.ui.main

import androidx.lifecycle.viewModelScope
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.network.bean.Release
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.ProxyInfo
import io.github.v2compose.ui.BaseViewModel
import io.github.v2compose.usecase.CheckForUpdatesUseCase
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
    private val checkForUpdates: CheckForUpdatesUseCase,
    private val checkIn: CheckInUseCase,
    private val appPreferences: AppPreferences,
    private val accountRepository: AccountRepository,
    private val mainPlatformDelegate: MainPlatformDelegate,
    val loadNodes: LoadNodesUseCase,
) : BaseViewModel() {

    private val _newRelease = MutableStateFlow(Release.Empty)
    val newRelease = _newRelease.asStateFlow()

    val unreadNotifications = accountRepository.unreadNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0,
        )

    init {
        autoCheckForUpdates()
        listenCanCheckIn()
        listenAutoCheckIn()
        initWebViewProxy()
    }

    private fun autoCheckForUpdates() {
        viewModelScope.launch {
            _newRelease.emit(checkForUpdates())
        }
    }

    private fun listenCanCheckIn() {
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
                    mainPlatformDelegate.syncAutoCheckIn(shouldCheckIn)
                }
        }
    }

    private fun initWebViewProxy() {
        viewModelScope.launch {
            appPreferences.proxyInfo.collectLatest { proxyInfo ->
                if (proxyInfo != ProxyInfo.Default) {
                    mainPlatformDelegate.updateWebViewProxy(proxyInfo)
                }
            }
        }
    }

    fun resetNewRelease() {
        viewModelScope.launch {
            _newRelease.emit(Release.Empty)
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
}
