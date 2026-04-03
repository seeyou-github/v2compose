package io.github.v2compose.ui.main.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RecentViewModel (
    newsRepository: NewsRepository,
    appPreferences: AppPreferences
) : ViewModel() {

    val recentTopics = newsRepository.recentTopics.cachedIn(viewModelScope)
    val topicTitleOverview = appPreferences.appSettings.map { it.topicTitleOverview }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

}