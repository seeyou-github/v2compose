package io.github.v2compose.ui.main.mine.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MyTopicsViewModel(
    private val accountRepository: AccountRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    val topicTitleOverview: StateFlow<Boolean> =
        appPreferences.appSettings.map { it.topicTitleOverview }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val myTopics = accountRepository.myTopics.cachedIn(viewModelScope)

}