package io.github.v2compose.ui.main.home.node

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.v2compose.network.bean.NodeTopicInfo
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HomeNodeTab(
    nodeName: String,
    nodeTitle: String,
    onTopicIdClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val viewModel: HomeNodeTabViewModel = koinViewModel(key = "homeNode:$nodeName") {
        parametersOf(nodeName)
    }
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val items = viewModel.nodeTopicItems.collectAsLazyPagingItems()
    val listState = items.rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier,
    ) {
        pagingRefreshItem(items)
        items(
            count = items.itemCount,
            key = { index ->
                when (val any = items[index]) {
                    is NodeTopicInfo -> "header"
                    is NodeTopicInfo.Item -> "topic:${any.topicId}"
                    else -> "item:$index"
                }
            },
        ) { index ->
            val item = items[index]
            when (item) {
                is NodeTopicInfo -> {
                    // header row already shown in node screen; ignore here.
                }

                is NodeTopicInfo.Item -> {
                    SimpleTopic(
                        title = item.title,
                        userName = item.userName,
                        userAvatar = item.avatarUrl,
                        time = "",
                        replyCount = item.commentNum.toString(),
                        nodeName = nodeName,
                        nodeTitle = nodeTitle,
                        titleOverview = topicTitleOverview,
                        hideUserInfo = appSettings.hideTopicUserInfo,
                        onItemClick = { onTopicIdClick(item.topicId) },
                        onNodeClick = { onNodeClick(nodeName, nodeTitle) },
                        onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatarUrl) },
                    )
                }

                null -> Unit
                else -> Unit
            }
        }
        pagingAppendMoreItem(items)
    }
}
