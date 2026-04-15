package io.github.v2compose.ui.login.twostep

import androidx.lifecycle.SavedStateHandle
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.v2compose.network.bean.TwoStepLoginInfo
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.usecase.UpdateAccountUseCase
import io.github.v2compose.util.KLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TwoStepLoginArgs(val once: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle.get<String>(twoStepArgsOnce).orEmpty(),
    )
}

class TwoStepLoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val updateAccount: UpdateAccountUseCase,
) : ViewModel() {
    companion object {
        private const val TAG = "TwoStepLoginViewModel"
    }

    private val args = TwoStepLoginArgs(savedStateHandle)
    private val seededInfo = args.once
        .takeIf { it.isNotBlank() }
        ?.let(::seedTwoStepLoginInfo)

    private val _twoStepLoginUiState =
        MutableStateFlow<TwoStepLoginUiState>(
            seededInfo?.let(TwoStepLoginUiState::Success) ?: TwoStepLoginUiState.Loading
        )
    val twoStepLoginUiState = _twoStepLoginUiState.asStateFlow()

    private val _login = MutableStateFlow<LoginState>(LoginState.Idle)
    val login = _login.asStateFlow()

    init {
        if (seededInfo == null) {
            fetchTwoStepLoginInfo()
        } else {
            KLogger.d(TAG, "skip initial /2fa fetch because once is provided by route")
        }
    }

    fun fetchTwoStepLoginInfo() {
        viewModelScope.launch {
            KLogger.d(TAG, "fetchTwoStepLoginInfo, onceFromArgs=${args.once.isNotBlank()}")
            _twoStepLoginUiState.emit(TwoStepLoginUiState.Loading)
            try {
                val result = accountRepository.getTwoStepLoginInfo()
                _twoStepLoginUiState.emit(TwoStepLoginUiState.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                _twoStepLoginUiState.emit(TwoStepLoginUiState.Error(e))
            }
        }
    }

    fun loginNextStep(code: String) {
        val uiState = _twoStepLoginUiState.value
        if (uiState !is TwoStepLoginUiState.Success) {
            return
        }
        viewModelScope.launch {
            _login.emit(LoginState.Loading)
            try {
                val result = accountRepository.loginNextStep(uiState.twoStepLoginInfo.once, code)
                _login.emit(LoginState.Idle)
                _twoStepLoginUiState.emit(TwoStepLoginUiState.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                _login.emit(LoginState.Error(e))
            }
        }
    }

    fun resetLoginState() {
        viewModelScope.launch {
            _login.emit(LoginState.Idle)
        }
    }

}

private fun seedTwoStepLoginInfo(once: String): TwoStepLoginInfo =
    TwoStepLoginInfo().apply {
        this.once = once
        title = "两步验证"
        avatar = "seeded"
    }

@Stable
sealed interface TwoStepLoginUiState {
    data class Success(val twoStepLoginInfo: TwoStepLoginInfo) : TwoStepLoginUiState
    object Loading : TwoStepLoginUiState
    data class Error(val error: Throwable) : TwoStepLoginUiState
}

@Stable
sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Error(val error: Throwable?) : LoginState
}
