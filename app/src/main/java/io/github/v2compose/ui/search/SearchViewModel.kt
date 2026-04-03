package io.github.v2compose.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.repository.TopicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel (
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder,
    private val topicRepository: TopicRepository,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val searchArgs = SearchArgs(savedStateHandle, stringDecoder)

    private val _keyword = MutableStateFlow(searchArgs.keyword)
    val keyword: StateFlow<String?> = _keyword.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyKeywords = appPreferences.appSettings.mapLatest { it.searchKeywords }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val topics = keyword.filterNot { it.isNullOrEmpty() }
        .flatMapLatest {
            topicRepository.search(it!!)
        }
        .cachedIn(viewModelScope)

    fun search(value: String) {
        viewModelScope.launch {
            _keyword.emit(value)
            val searchKeywords = historyKeywords.value.toMutableList()
                .also {
                    it.remove(value)
                    it.add(0, value)
                }
            appPreferences.searchKeywords(searchKeywords.take(10))
        }
    }

    fun clearHistoryKeywords() {
        viewModelScope.launch {
            appPreferences.searchKeywords(listOf())
        }
    }
}