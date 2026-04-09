package io.github.v2compose.ui.user

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.v2compose.Constants
import io.github.v2compose.V2exUri
import io.github.v2compose.core.share
import io.github.v2compose.network.bean.UserReplies
import io.github.v2compose.network.bean.UserTopics
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.HtmlContent
import io.github.v2compose.ui.common.ListDivider
import io.github.v2compose.ui.common.LoadError
import io.github.v2compose.ui.common.Loading
import io.github.v2compose.ui.common.NodeTag
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import io.github.v2compose.ui.gallery.composables.PopupImage
import io.github.v2compose.ui.user.composables.UserToolbar
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import v2compose.shared.generated.resources.*

private const val TAG = "UserScreen"

@Composable
fun UserScreenRoute(
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    viewModel: UserViewModel = koinViewModel(),
    screenState: UserScreenState = rememberUserScreenState(),
) {
    val context = LocalContext.current

    val userArgs = viewModel.userArgs
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val userUiState by viewModel.userUiState.collectAsStateWithLifecycle()
    val userTopics = viewModel.userTopics.collectAsLazyPagingItems()
    val userReplies = viewModel.userReplies.collectAsLazyPagingItems()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    var htmlImageUrl by rememberSaveable { mutableStateOf("") }

    if (htmlImageUrl.isNotEmpty()) {
        PopupImage(imageUrl = htmlImageUrl) {
            htmlImageUrl = ""
        }
    }

    HandleSnackbarMessage(viewModel, screenState)

    UserScreen(
        userUiState = userUiState,
        userTopics = userTopics,
        userReplies = userReplies,
        topicTitleOverview = topicTitleOverview,
        isLoggedIn = isLoggedIn,
        sizedHtmls = viewModel.sizedHtmls,
        onBackClick = onBackClick,
        onShareClick = {
            context.share(userArgs.userName, V2exUri.userUrl(userArgs.userName))
        },
        onRetryClick = { viewModel.retry() },
        onFollowClick = viewModel::followUser,
        onBlockClick = viewModel::blockUser,
        onTopicClick = onTopicClick,
        onNodeClick = onNodeClick,
        openUri = openUri,
        loadHtmlImage = viewModel::loadHtmlImage,
        onHtmlImageClick = { current, _ -> htmlImageUrl = current },
    )
}

@Composable
private fun UserScreen(
    userUiState: UserUiState,
    userTopics: LazyPagingItems<UserTopics.Item>,
    userReplies: LazyPagingItems<UserReplies.Item>,
    topicTitleOverview: Boolean,
    isLoggedIn: Boolean,
    sizedHtmls: SnapshotStateMap<String, String>,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onRetryClick: () -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    val scaffoldState = rememberCollapsingToolbarScaffoldState()

    Surface(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        CollapsingToolbarScaffold(modifier = Modifier.fillMaxSize(),
            state = scaffoldState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            enabled = true,
            toolbar = {
                UserToolbar(
                    userUiState = userUiState,
                    isLoggedIn = isLoggedIn,
                    scaffoldState = scaffoldState,
                    onBackClick = onBackClick,
                    onShareClick = onShareClick,
                    onFollowClick = onFollowClick,
                    onBlockClick = onBlockClick,
                )
            }) {
            UserContent(
                userUiState = userUiState,
                userTopics = userTopics,
                userReplies = userReplies,
                sizedHtmls = sizedHtmls,
                topicTitleOverview = topicTitleOverview,
                onRetryClick = onRetryClick,
                onTopicClick = onTopicClick,
                onNodeClick = onNodeClick,
                openUri = openUri,
                loadHtmlImage = loadHtmlImage,
                onHtmlImageClick = onHtmlImageClick,
            )
        }
    }

}


@Composable
private fun UserContent(
    userUiState: UserUiState,
    userTopics: LazyPagingItems<UserTopics.Item>,
    userReplies: LazyPagingItems<UserReplies.Item>,
    topicTitleOverview: Boolean,
    sizedHtmls: SnapshotStateMap<String, String>,
    onRetryClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    when (userUiState) {
        is UserUiState.Success -> {
            UserPager(
                userTopics = userTopics,
                userReplies = userReplies,
                topicTitleOverview = topicTitleOverview,
                sizedHtmls = sizedHtmls,
                onTopicClick = onTopicClick,
                onNodeClick = onNodeClick,
                openUri = openUri,
                loadHtmlImage = loadHtmlImage,
                onHtmlImageClick = onHtmlImageClick,
            )
        }

        is UserUiState.Loading -> {
            Loading()
        }

        is UserUiState.Error -> {
            LoadError(error = userUiState.error, onRetryClick = onRetryClick)
        }
    }
}

@Composable
fun UserPager(
    userTopics: LazyPagingItems<UserTopics.Item>,
    userReplies: LazyPagingItems<UserReplies.Item>,
    topicTitleOverview: Boolean,
    sizedHtmls: SnapshotStateMap<String, String>,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabNames = listOf(stringResource(Res.string.user_topic), stringResource(Res.string.user_reply))

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions: List<TabPosition> ->
                UserTabIndicator(tabPosition = tabPositions[pagerState.currentPage])
            }) {
            tabNames.forEachIndexed { index, name ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected, onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page = index)
                        }
                    }, modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        name,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                }
            }
        }

        HorizontalPager(state = pagerState) { index ->
            when (index) {
                0 -> UserTopicsList(
                    items = userTopics,
                    topicTitleOverview = topicTitleOverview,
                    onTopicClick = onTopicClick,
                    onNodeClick = onNodeClick
                )

                1 -> UserRepliesList(
                    items = userReplies,
                    sizedHtmls = sizedHtmls,
                    onTopicClick = onTopicClick,
                    openUri = openUri,
                    loadHtmlImage = loadHtmlImage,
                    onHtmlImageClick = onHtmlImageClick,
                )
            }
        }
    }
}

@Composable
private fun UserTopicsList(
    items: LazyPagingItems<UserTopics.Item>,
    topicTitleOverview: Boolean,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = items.rememberLazyListState()) {
        pagingRefreshItem(lazyPagingItems = items)

        items(items.itemCount, key = items.itemKey { it.link }) { index ->
            val item = items[index] ?: return@items
            UserTopicItem(
                topic = item,
                topicTitleOverview = topicTitleOverview,
                onTopicClick = onTopicClick,
                onNodeClick = onNodeClick
            )
        }

        pagingAppendMoreItem(lazyPagingItems = items)
    }
}

@Composable
fun UserTopicItem(
    topic: UserTopics.Item,
    topicTitleOverview: Boolean,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { onTopicClick(topic.link) }) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text(
                    topic.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = if (topicTitleOverview) Constants.topicTitleOverviewMaxLines else Integer.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                NodeTag(
                    nodeTitle = topic.nodeTitle,
                    nodeName = topic.nodeLink,
                    onItemClick = onNodeClick
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                topic.lastReply,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ListDivider(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun UserRepliesList(
    items: LazyPagingItems<UserReplies.Item>,
    sizedHtmls: SnapshotStateMap<String, String>,
    onTopicClick: (String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = items.rememberLazyListState()) {
        pagingRefreshItem(items)

        items(
            items.itemCount,
            key = items.itemKey { item -> "${item.dock.link}#${item.dock.time}#${item.content.content}" }) { index ->
            val item = items[index] ?: return@items
            val tag = "${item.dock.link}#${item.dock.time}#${item.content.content}"
            UserReplyItem(
                reply = item,
                content = sizedHtmls[tag] ?: item.content.content,
                onTopicClick = onTopicClick,
                openUri = openUri,
                loadHtmlImage = { html, src -> loadHtmlImage(tag, html, src) },
                onHtmlImageClick = onHtmlImageClick,
            )
        }

        pagingAppendMoreItem(items)
    }
}

@Composable
fun UserReplyItem(
    reply: UserReplies.Item,
    content: String,
    onTopicClick: (String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { onTopicClick(reply.dock.link) }) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                reply.dock.title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                reply.dock.time,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.align(Alignment.End),
            )
            Spacer(modifier = Modifier.height(8.dp))

            val backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            val leftBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            HtmlContent(
                content = content,
                onUriClick = { openUri(V2exUri.fixUriWithTopicPath(it, reply.dock.link)) },
                loadImage = loadHtmlImage,
                onHtmlImageClick = onHtmlImageClick,
                onClick = { onTopicClick(reply.dock.link) },
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = backgroundColor)
                        drawRect(
                            color = leftBorderColor, size = size.copy(width = 4.dp.toPx())
                        )
                    }
                    .padding(start = 8.dp),
            )
        }
        ListDivider(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun UserTabIndicator(tabPosition: TabPosition, modifier: Modifier = Modifier) {
    val tabWidth = 32.dp
    val leftSpace = (tabPosition.width - tabWidth) / 2
    val currentTabWidth by animateDpAsState(
        targetValue = tabWidth,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = tabPosition.left + leftSpace,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset(x = indicatorOffset)
            .width(currentTabWidth)
            .height(2.dp)
            .background(color = MaterialTheme.colorScheme.primary)
    )
}
