package io.github.v2compose.ui.user

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.network.bean.UserPageInfo
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.repository.UserRepository
import io.github.v2compose.ui.BaseViewModel
import io.github.v2compose.usecase.FixHtmlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.*

class UserViewModel (
    application: Application,
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder,
    private val userRepository: UserRepository,
    private val topicRepository: TopicRepository,
    private val accountRepository: AccountRepository,
    private val fixedHtmlImage: FixHtmlUseCase,
) : BaseViewModel() {

    private val context = application.applicationContext

    val userArgs = UserArgs(savedStateHandle, stringDecoder)

    private val _userUiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val userUiState = _userUiState.asStateFlow()

    val userTopics = userRepository.getUserTopics(userArgs.userName).cachedIn(viewModelScope)

    val userReplies = userRepository.getUserReplies(userArgs.userName).cachedIn(viewModelScope)

    val topicTitleOverview: StateFlow<Boolean> = topicRepository.topicTitleOverview.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = true,
    )

    val isLoggedIn = accountRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    init {
        loadUserPageInfo()
    }

    fun retry() {
        loadUserPageInfo()
    }

    private fun loadUserPageInfo() {
        viewModelScope.launch {
            _userUiState.emit(UserUiState.Loading)
            try {
                val result = userRepository.getUserPageInfo(userArgs.userName)
                _userUiState.emit(UserUiState.Success(result))
            } catch (e: Exception) {
                e.printStackTrace()
                _userUiState.emit(UserUiState.Error(e))
            }
        }
    }

    fun followUser() {
        doUserAction { it.getFollowUrl() ?: ""}
    }

    fun blockUser() {
        doUserAction { it.getBlockUrl() ?: ""}
    }

    private fun doUserAction(actionUrl: (UserPageInfo) -> String) {
        if (userUiState.value !is UserUiState.Success) {
            return
        }
        val userPageInfo = (userUiState.value as UserUiState.Success).userPageInfo
        val url = actionUrl(userPageInfo)
        viewModelScope.launch {
            try {
                val result = userRepository.doUserAction(userPageInfo.userName, url)
                _userUiState.emit(UserUiState.Success(result))
//                updateSnackbarMessage(Res.string.user_action_success)
            } catch (e: Exception) {
                e.printStackTrace()
                updateSnackbarMessage(e.message ?: getString(Res.string.user_action_failure))
            }
        }
    }

    val sizedHtmls = mutableStateMapOf<String, String>()

    fun loadHtmlImage(tag: String, html: String, imageSrc: String?) {
        viewModelScope.launch {
            fixedHtmlImage.loadHtmlImages(html, imageSrc).collectLatest { sizedHtmls[tag] = it }
        }
    }

}

@Stable
sealed interface UserUiState {
    data class Success(val userPageInfo: UserPageInfo) : UserUiState
    object Loading : UserUiState
    data class Error(val error: Throwable) : UserUiState
}