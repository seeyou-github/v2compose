package io.github.v2compose.repository

import androidx.paging.PagingData
import io.github.v2compose.shared.bean.ContentFormat
import io.github.v2compose.shared.bean.DraftTopic
import io.github.v2compose.network.bean.AppendTopicPageInfo
import io.github.v2compose.network.bean.CreateTopicPageInfo
import io.github.v2compose.network.bean.ReplyTopicResultInfo
import io.github.v2compose.network.bean.SoV2EXSearchResultInfo
import io.github.v2compose.network.bean.TopicInfo
import io.github.v2compose.shared.bean.TopicNode
import io.github.v2compose.network.bean.V2exResult
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    suspend fun getTopicInfo(topicId: String): TopicInfo
    fun getTopic(topicId: String, initialPage: Int?, reversed: Boolean): Flow<PagingData<Any>>

    val repliesOrderReversed: Flow<Boolean>

    suspend fun toggleRepliesReversed()

    val highlightOpReply: Flow<Boolean>

    fun search(keyword: String): Flow<PagingData<SoV2EXSearchResultInfo.Hit>>

    val topicTitleOverview: Flow<Boolean>

    val replyWithFloor: Flow<Boolean>

    suspend fun doTopicAction(
        action: String,
        method: ActionMethod,
        topicId: String,
        once: String
    ): V2exResult

    suspend fun doReplyAction(
        action: String,
        method: ActionMethod,
        topicId: String,
        replyId: String,
        once: String
    ): V2exResult

    suspend fun ignoreReply(topicId: String, replyId: String, once: String): Boolean

    suspend fun replyTopic(topicId: String, content: String, once: String): ReplyTopicResultInfo

    val draftTopic: Flow<DraftTopic>

    suspend fun saveDraftTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        node: TopicNode?
    )

    suspend fun getCreateTopicPageInfo(): CreateTopicPageInfo

    suspend fun getTopicNodes(): List<TopicNode>

    suspend fun createTopic(
        title: String,
        content: String,
        contentFormat: ContentFormat,
        nodeName: String,
        once: String
    ): CreateTopicPageInfo

    suspend fun getAppendTopicPageInfo(topicId: String): AppendTopicPageInfo

    suspend fun addSupplement(
        topicId: String,
        supplement: String,
        contentFormat: ContentFormat,
        once: String
    ): AppendTopicPageInfo
}

enum class ActionMethod {
    Get, Post
}