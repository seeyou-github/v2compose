package io.github.v2compose.ui.login.google

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.shared.bean.Account
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class GoogleLoginArgs(val once: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle.get<String>(argsOnce).orEmpty(),
    )
}

class GoogleLoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    val args = GoogleLoginArgs(savedStateHandle)

    val account = accountRepository.account
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Account.Empty)

    suspend fun fetchUserInfo() {
        try {
            accountRepository.fetchUserInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
