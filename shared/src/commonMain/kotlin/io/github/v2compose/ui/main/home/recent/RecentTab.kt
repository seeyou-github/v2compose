package io.github.v2compose.ui.main.home.recent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.v2compose.network.bean.RecentTopics
import io.github.v2compose.ui.common.PagingLoadState
import io.github.v2compose.ui.common.PullToRefresh
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingPrependMoreItem
import io.github.v2compose.ui.common.rememberLazyListState
import io.github.v2compose.ui.main.composables.ClickHandler
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecentTab(
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    viewModel: RecentViewModel = koinViewModel(),
    nestedScrollConnection: NestedScrollConnection? = null,
) {

    val recentTopics = viewModel.recentTopics.collectAsLazyPagingItems()
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (recentTopics.itemSnapshotList.isEmpty()) {
            PagingLoadState(
                state = recentTopics.loadState.refresh,
                onRetryClick = { recentTopics.retry() },
            )
        } else {
            RecentTopicsList(
                recentTopics = recentTopics,
                topicTitleOverview = topicTitleOverview,
                hideUserInfo = appSettings.hideTopicUserInfo,
                onRecentItemClick = onRecentItemClick,
                onNodeClick = onNodeClick,
                onUserAvatarClick = onUserAvatarClick,
                nestedScrollConnection = nestedScrollConnection,
            )
        }
    }
}

@Composable
private fun RecentTopicsList(
    recentTopics: LazyPagingItems<RecentTopics.Item>,
    topicTitleOverview: Boolean,
    hideUserInfo: Boolean,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val refreshing = remember(recentTopics.loadState) {
        recentTopics.loadState.refresh is LoadState.Loading
    }

    PullToRefresh(refreshing = refreshing, onRefresh = { recentTopics.refresh() }) {
        val lazyListState = recentTopics.rememberLazyListState()
        ClickHandler(enabled = !refreshing) {
            coroutineScope.launch {
                if (lazyListState.isScrollInProgress) {
                    lazyListState.animateScrollToItem(0)
                    recentTopics.refresh()
                } else if (lazyListState.canScrollBackward) {
                    lazyListState.animateScrollToItem(0)
                } else {
                    recentTopics.refresh()
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = if (nestedScrollConnection != null) {
                Modifier.nestedScroll(nestedScrollConnection).fillMaxSize()
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            pagingPrependMoreItem(recentTopics)
            items(recentTopics.itemCount, key = recentTopics.itemKey { it.id }) { index ->
                val item = recentTopics[index] ?: return@items
                SimpleTopic(
                    title = item.title,
                    userName = item.userName,
                    userAvatar = item.avatar,
                    time = item.time,
                    replyCount = item.replies.toString(),
                    nodeName = item.nodeName,
                    nodeTitle = item.nodeTitle,
                    titleOverview = topicTitleOverview,
                    hideUserInfo = hideUserInfo,
                    onItemClick = { onRecentItemClick(item) },
                    onNodeClick = { onNodeClick(item.nodeName, item.nodeTitle) },
                    onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatar) })
            }
            pagingAppendMoreItem(recentTopics)
        }
    }
}
