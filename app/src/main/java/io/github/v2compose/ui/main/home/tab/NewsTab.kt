package io.github.v2compose.ui.main.home.tab

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.ui.common.LoadMore
import io.github.v2compose.ui.common.PullToRefresh
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.main.composables.ClickHandler
import io.github.v2compose.ui.main.home.NewsTabInfo
import io.github.v2compose.util.L
import io.github.v2compose.util.Logf
import kotlinx.coroutines.launch

private const val TAG = "NewTab"

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NewsTab(
    newsTabInfo: NewsTabInfo,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    val viewModel: NewsViewModel = hiltViewModel<NewsViewModel, NewsViewModel.Factory>(
        key = newsTabInfo.value, // 关键：不同的 key 会创建不同的实例
        creationCallback = { factory -> factory.create(newsTabInfo.value) }
    )
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()

    val newsUiState by viewModel.newsUiState.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    NewsContent(
        refreshing = refreshing,
        newsUiState = newsUiState,
        topicTitleOverview = topicTitleOverview,
        onNewsItemClick = onNewsItemClick,
        onRefreshList = { viewModel.refresh() },
        onRetryClick = { viewModel.retry() },
        onNodeClick = onNodeClick,
        onUserAvatarClick = onUserAvatarClick,
    )
}

@Composable
fun NewsContent(
    refreshing: Boolean,
    newsUiState: NewsUiState,
    topicTitleOverview: Boolean,
    onNewsItemClick: ((NewsInfo.Item) -> Unit),
    onNodeClick: (String, String) -> Unit,
    onRetryClick: () -> Unit,
    onRefreshList: () -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (newsUiState) {
            is NewsUiState.Success -> {
                NewsList(
                    refreshing = refreshing,
                    newsInfo = newsUiState.data,
                    topicTitleOverview = topicTitleOverview,
                    onRefresh = onRefreshList,
                    onNewsItemClick = onNewsItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                )
            }

            else -> {
                LoadMore(
                    hasError = newsUiState is NewsUiState.Error,
                    error = if (newsUiState is NewsUiState.Error) newsUiState.error else null,
                    onRetryClick = onRetryClick
                )
            }
        }
    }

}

@Composable
private fun NewsList(
    refreshing: Boolean,
    newsInfo: NewsInfo,
    topicTitleOverview: Boolean,
    onRefresh: () -> Unit,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    PullToRefresh(refreshing = refreshing, onRefresh = onRefresh) {
        val lazyListState = rememberLazyListState()

        ClickHandler(enabled = !refreshing) {
            coroutineScope.launch {
                if (lazyListState.isScrollInProgress) {
                    lazyListState.animateScrollToItem(0)
                    onRefresh()
                } else if (lazyListState.canScrollBackward) {
                    lazyListState.animateScrollToItem(0)
                } else {
                    onRefresh()
                }
            }
        }

        LazyColumn(state = lazyListState) {
            items(newsInfo.items, key = { it.id }) { item ->
                val tagId = item.tagId()
                if(tagId.isNullOrBlank()){
                    L.e("topic's item, tagId is null or blank, item = $item")
                    return@items
                }
                SimpleTopic(
                    title = item.title,
                    userName = item.userName,
                    userAvatar = item.avatar,
                    time = item.time,
                    replyCount = item.replies.toString(),
                    nodeName = tagId,
                    nodeTitle = item.tagName,
                    titleOverview = topicTitleOverview,
                    onItemClick = { onNewsItemClick(item) },
                    onNodeClick = { onNodeClick(tagId, item.tagName) },
                    onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatar) }
                )
            }
        }
    }
}
