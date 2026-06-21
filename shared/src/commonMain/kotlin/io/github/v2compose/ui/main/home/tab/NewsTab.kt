package io.github.v2compose.ui.main.home.tab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.ui.common.LoadMore
import io.github.v2compose.ui.common.PullToRefresh
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.main.composables.ClickHandler
import io.github.v2compose.ui.main.home.NewsTabInfo
import io.github.v2compose.util.KLogger
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NewsTab(
    newsTabInfo: NewsTabInfo,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val viewModel: NewsViewModel = koinViewModel(
        key = newsTabInfo.value
    ) {
        parametersOf(newsTabInfo.value)
    }
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val newsUiState by viewModel.newsUiState.collectAsStateWithLifecycle()
    val refreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    NewsContent(
        refreshing = refreshing,
        newsUiState = newsUiState,
        topicTitleOverview = topicTitleOverview,
        hideUserInfo = appSettings.hideTopicUserInfo,
        onNewsItemClick = onNewsItemClick,
        onRefreshList = { viewModel.refresh() },
        onRetryClick = { viewModel.retry() },
        onNodeClick = onNodeClick,
        onUserAvatarClick = onUserAvatarClick,
        nestedScrollConnection = nestedScrollConnection,
    )
}

@Composable
fun NewsContent(
    refreshing: Boolean,
    newsUiState: NewsUiState,
    topicTitleOverview: Boolean,
    hideUserInfo: Boolean,
    onNewsItemClick: ((NewsInfo.Item) -> Unit),
    onNodeClick: (String, String) -> Unit,
    onRetryClick: () -> Unit,
    onRefreshList: () -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (newsUiState) {
            is NewsUiState.Success -> {
                NewsList(
                    refreshing = refreshing,
                    newsInfo = newsUiState.data,
                    topicTitleOverview = topicTitleOverview,
                    hideUserInfo = hideUserInfo,
                    onRefresh = onRefreshList,
                    onNewsItemClick = onNewsItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    nestedScrollConnection = nestedScrollConnection,
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
    hideUserInfo: Boolean,
    onRefresh: () -> Unit,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
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

        LazyColumn(
            state = lazyListState,
            modifier = if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier,
        ) {
            items(newsInfo.items, key = { it.id }) { item ->
                val tagId = item.tagId()
                if (tagId.isNullOrBlank()) {
                    KLogger.e("NewsTab", "topic item tagId is null or blank, item = $item")
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
                    hideUserInfo = hideUserInfo,
                    onItemClick = { onNewsItemClick(item) },
                    onNodeClick = { onNodeClick(tagId, item.tagName) },
                    onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatar) }
                )
            }
        }
    }
}
