package io.github.v2compose.ui.main.mine.following

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.v2compose.network.bean.MyFollowingInfo
import io.github.v2compose.ui.common.BackIcon
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import v2compose.shared.generated.resources.*

private const val TAG = "MyTopicsScreen"

@Composable
fun MyFollowingScreenRoute(
    onBackClick: () -> Unit,
    onTopicClick: (MyFollowingInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    viewModel: MyFollowingViewModel = koinViewModel()
) {
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val myFollowing = viewModel.myFollowing.collectAsLazyPagingItems()

    MyFollowingScreen(
        topicTitleOverview = topicTitleOverview,
        myFollowing = myFollowing,
        onBackClick = onBackClick,
        onTopicClick = onTopicClick,
        onNodeClick = onNodeClick,
        onUserAvatarClick = onUserAvatarClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyFollowingScreen(
    topicTitleOverview: Boolean,
    myFollowing: LazyPagingItems<MyFollowingInfo.Item>,
    onBackClick: () -> Unit,
    onTopicClick: (MyFollowingInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(Res.string.my_following)) },
                navigationIcon = { BackIcon(onBackClick = onBackClick) },
                scrollBehavior = scrollBehavior
            )
        },
    ) { insets ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            MyFollowingTopicList(
                myFollowing = myFollowing,
                topicTitleOverview = topicTitleOverview,
                onTopicClick = onTopicClick,
                onNodeClick = onNodeClick,
                onUserAvatarClick = onUserAvatarClick
            )
        }
    }
}

@Composable
private fun MyFollowingTopicList(
    myFollowing: LazyPagingItems<MyFollowingInfo.Item>,
    topicTitleOverview: Boolean,
    onTopicClick: (MyFollowingInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit
) {
    val lazyListState = myFollowing.rememberLazyListState()
    LazyColumn(state = lazyListState) {
        pagingRefreshItem(myFollowing)
        items(myFollowing.itemCount, key = myFollowing.itemKey { it.getId() }) { index ->
            val item = myFollowing[index]
            item?.let {
                Log.d(TAG, "myfollowing, index = $index, item = $item")
                SimpleTopic(
                    title = item.title,
                    userName = item.userName,
                    userAvatar = item.avatar,
                    time = item.time,
                    replyCount = item.commentNum.toString(),
                    nodeName = item.tagTitle,
                    nodeTitle = item.tagTitle,
                    titleOverview = topicTitleOverview,
                    onItemClick = { onTopicClick(item) },
                    onNodeClick = { onNodeClick(item.tagTitle, item.tagTitle) },
                    onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatar) }
                )
            }
        }
        pagingAppendMoreItem(myFollowing)
    }
}