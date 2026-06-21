package io.github.v2compose.ui.main.home.node

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.datasource.AppPreferences
import io.github.v2compose.repository.NodeRepository
import io.github.v2compose.repository.TopicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeNodeTabViewModel(
    private val nodeName: String,
    private val nodeRepository: NodeRepository,
    private val topicRepository: TopicRepository,
    appPreferences: AppPreferences,
) : ViewModel() {

    val nodeTopicItems = nodeRepository.getNodeTopicInfo(nodeName).cachedIn(viewModelScope)

    val topicTitleOverview: StateFlow<Boolean> = topicRepository.topicTitleOverview
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val appSettings = appPreferences.appSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        io.github.v2compose.shared.bean.AppSettings.Default,
    )
}
