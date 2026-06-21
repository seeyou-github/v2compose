package io.github.v2compose.ui.main.home.tab

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.repository.NewsRepository
import io.github.v2compose.repository.TopicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NewsViewModel(
    val tab: String,
    private val newsRepository: NewsRepository,
    private val topicRepository: TopicRepository,
    appPreferences: AppPreferences,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _newsInfoFlow = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsUiState = _newsInfoFlow.asStateFlow()

    val topicTitleOverview: StateFlow<Boolean> = topicRepository.topicTitleOverview.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = true,
    )

    val appSettings = appPreferences.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        initialValue = io.github.v2compose.shared.bean.AppSettings.Default,
    )

    init {
        load()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            loadInternal()
            _isRefreshing.emit(false)
        }
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _newsInfoFlow.emit(NewsUiState.Loading)
            loadInternal()
        }
    }

    private suspend fun loadInternal() {
        try {
            val newsInfo = newsRepository.getHomeNews(tab)
            _newsInfoFlow.emit(NewsUiState.Success(newsInfo))
        } catch (e: Exception) {
            e.printStackTrace()
            _newsInfoFlow.emit(NewsUiState.Error(e))
        }
    }

}

@Stable
sealed interface NewsUiState {
    object Loading : NewsUiState
    data class Success(val data: NewsInfo) : NewsUiState
    data class Error(val error: Throwable?) : NewsUiState
}
