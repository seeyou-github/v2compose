package io.github.v2compose.ui.write

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.extension.isRedirect
import io.github.v2compose.core.extension.redirectLocation
import io.github.v2compose.network.bean.CreateTopicPageInfo
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.shared.bean.ContentFormat
import io.github.v2compose.shared.bean.DraftTopic
import io.github.v2compose.shared.bean.TopicNode
import io.github.v2compose.usecase.LoadNodesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WriteTopicViewModel(
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder,
    private val topicRepository: TopicRepository,
    val loadNodes: LoadNodesUseCase,
) : ViewModel() {

    val writeTopicArgs: WriteTopicArgs = WriteTopicArgs(savedStateHandle, stringDecoder)

    private val _createTopicState = MutableStateFlow<CreateTopicState>(CreateTopicState.Idle)
    val createTopicState: StateFlow<CreateTopicState> = _createTopicState

    val draftTopic: DraftTopic
        get() = runBlocking {
            topicRepository.draftTopic.first().let { local ->
                if (local.node != null) local else
                    local.copy(
                        node = TopicNode(
                            writeTopicArgs.nodeName ?: "",
                            writeTopicArgs.nodeTitle ?: "",
                        )
                    )
            }
        }

    init {
        loadNodes()
    }

    fun loadNodes() {
        viewModelScope.launch {
            loadNodes.execute()
        }
    }

    fun createTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        nodeName: String
    ) {
        viewModelScope.launch {
            val once: String =
                _createTopicState.value.let { if (it is CreateTopicState.Failure) it.pageInfo.once else "" }
            _createTopicState.emit(CreateTopicState.Loading)
            try {
                val currentOnce = once.ifEmpty {
                    val pageInfo = topicRepository.getCreateTopicPageInfo()
                    if (pageInfo.once.isNullOrEmpty()) {
                        _createTopicState.emit(CreateTopicState.Error(null))
                        return@launch
                    }
                    pageInfo.once
                }
                val result = topicRepository.createTopic(
                    title,
                    content,
                    contentFormat,
                    nodeName,
                    currentOnce
                )
                _createTopicState.emit(CreateTopicState.Failure(result))
            } catch (e: Exception) {
                e.printStackTrace()
                if (e.isRedirect) {
                    saveDraftTopic("", "", ContentFormat.Original, null)
                    val location = e.redirectLocation ?: ""
                    val topicId = Uri.parse(location).pathSegments.getOrNull(1)
                    if (topicId != null) {
                        _createTopicState.emit(CreateTopicState.Success(topicId))
                        return@launch
                    }
                }
                _createTopicState.emit(CreateTopicState.Error(e))
            }
        }
    }

    fun saveDraftTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        node: TopicNode?
    ) {
        viewModelScope.launch {
            topicRepository.saveDraftTopic(title, content, contentFormat, node)
        }
    }

}

@Stable
sealed interface CreateTopicState {
    object Idle : CreateTopicState
    object Loading : CreateTopicState
    data class Success(val topicId: String) : CreateTopicState
    data class Failure(val pageInfo: CreateTopicPageInfo) : CreateTopicState
    data class Error(val error: Throwable?) : CreateTopicState
}
