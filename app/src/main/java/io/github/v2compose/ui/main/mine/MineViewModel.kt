package io.github.v2compose.ui.main.mine

import android.app.Application
import androidx.lifecycle.viewModelScope
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.Account
import io.github.v2compose.ui.BaseViewModel
import io.github.v2compose.usecase.CheckInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.daily_mission_failure

class MineViewModel(
    application: Application,
    private val checkIn: CheckInUseCase,
    private val accountRepository: AccountRepository,
) : BaseViewModel() {

    private val context = application.applicationContext

    val account: StateFlow<Account> = accountRepository.account
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            Account.Empty
        )

    val hasCheckingInTips: StateFlow<Boolean> = accountRepository.hasCheckingInTips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val lastCheckInTime: StateFlow<Long> = accountRepository.lastCheckInTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0L)

    private val _checkingIn = MutableStateFlow(false)
    val checkingIn = _checkingIn.asStateFlow()

    private var lastRefreshAccountTime = 0L

    init {
        listenAccount()
    }

    private fun listenAccount() {
        viewModelScope.launch {
            account.map { it.userName }
                .distinctUntilChanged()
                .collectLatest {
                    if (it.isNotEmpty()) refreshAccountInternal(force = true)
                }
        }
    }

    fun refreshAccount() {
        viewModelScope.launch {
            refreshAccountInternal()
        }
    }

    private suspend fun refreshAccountInternal(force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!force && currentTime - lastRefreshAccountTime < 5 * 60 * 1000) {
            return
        }
        lastRefreshAccountTime = currentTime

        try {
            accountRepository.refreshAccount()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //日常任务：领取每日登录奖励
    fun doCheckIn() {
        viewModelScope.launch {
            _checkingIn.emit(true)
            val result = checkIn()
            if (result.success) {
                result.message?.let { updateSnackbarMessage(it) }
            } else {
                updateSnackbarMessage(
                    result.message ?: getString(Res.string.daily_mission_failure)
                )
            }
            _checkingIn.emit(false)
        }
    }

}

