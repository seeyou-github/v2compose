package io.github.v2compose.ui.user

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.v2compose.V2exUri
import io.github.v2compose.Constants
import io.github.v2compose.network.bean.UserPageInfo
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
import io.github.v2compose.ui.user.composables.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.user_reply
import v2compose.shared.generated.resources.user_topic

@Composable
fun UserScreenRoute(
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    onShareUser: (String, String) -> Unit,
    viewModel: UserViewModel = koinViewModel(),
) {
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

    HandleSnackbarMessage(viewModel)

    UserScreen(
        userUiState = userUiState,
        userTopics = userTopics,
        userReplies = userReplies,
        topicTitleOverview = topicTitleOverview,
        isLoggedIn = isLoggedIn,
        sizedHtmls = viewModel.sizedHtmls,
        onBackClick = onBackClick,
        onShareClick = {
            onShareUser(userArgs.userName, V2exUri.userUrl(userArgs.userName))
        },
        onRetryClick = viewModel::retry,
        onFollowClick = viewModel::followUser,
        onBlockClick = viewModel::blockUser,
        onTopicClick = onTopicClick,
        onNodeClick = onNodeClick,
        openUri = openUri,
        loadHtmlImage = viewModel::loadHtmlImage,
        onHtmlImageClick = { current, _ -> htmlImageUrl = current },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topicsListState = userTopics.rememberLazyListState()
    val repliesListState = userReplies.rememberLazyListState()
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    var headerHeightPx by remember { mutableIntStateOf(1) }
    val headerAlpha by remember(userUiState, selectedTabIndex, headerHeightPx) {
        derivedStateOf {
            if (userUiState !is UserUiState.Success) {
                0f
            } else {
                val listState = if (selectedTabIndex == 0) topicsListState else repliesListState
                calculateHeaderAlpha(
                    listState = listState,
                    headerHeightPx = headerHeightPx,
                )
            }
        }
    }

    Surface(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                UserToolbar(
                    userUiState = userUiState,
                    isLoggedIn = isLoggedIn,
                    titleAlpha = 1f - headerAlpha,
                    scrollBehavior = scrollBehavior,
                    onBackClick = onBackClick,
                    onShareClick = onShareClick,
                    onFollowClick = onFollowClick,
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                UserContent(
                    userUiState = userUiState,
                    userTopics = userTopics,
                    userReplies = userReplies,
                    sizedHtmls = sizedHtmls,
                    topicTitleOverview = topicTitleOverview,
                    isLoggedIn = isLoggedIn,
                    topicsListState = topicsListState,
                    repliesListState = repliesListState,
                    selectedTabIndex = selectedTabIndex,
                    headerAlpha = headerAlpha,
                    onRetryClick = onRetryClick,
                    onSelectedTabIndexChange = { selectedTabIndex = it },
                    onHeaderMeasured = { headerHeightPx = it },
                    onFollowClick = onFollowClick,
                    onBlockClick = onBlockClick,
                    onTopicClick = onTopicClick,
                    onNodeClick = onNodeClick,
                    openUri = openUri,
                    loadHtmlImage = loadHtmlImage,
                    onHtmlImageClick = onHtmlImageClick,
                )
            }
        }
    }
}


@Composable
private fun UserContent(
    userUiState: UserUiState,
    userTopics: LazyPagingItems<UserTopics.Item>,
    userReplies: LazyPagingItems<UserReplies.Item>,
    topicTitleOverview: Boolean,
    isLoggedIn: Boolean,
    topicsListState: LazyListState,
    repliesListState: LazyListState,
    selectedTabIndex: Int,
    headerAlpha: Float,
    sizedHtmls: SnapshotStateMap<String, String>,
    onRetryClick: () -> Unit,
    onSelectedTabIndexChange: (Int) -> Unit,
    onHeaderMeasured: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    when (userUiState) {
        is UserUiState.Success -> {
            UserPager(
                userPageInfo = userUiState.userPageInfo,
                userTopics = userTopics,
                userReplies = userReplies,
                topicTitleOverview = topicTitleOverview,
                isLoggedIn = isLoggedIn,
                topicsListState = topicsListState,
                repliesListState = repliesListState,
                selectedTabIndex = selectedTabIndex,
                headerAlpha = headerAlpha,
                sizedHtmls = sizedHtmls,
                onSelectedTabIndexChange = onSelectedTabIndexChange,
                onHeaderMeasured = onHeaderMeasured,
                onFollowClick = onFollowClick,
                onBlockClick = onBlockClick,
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
    userPageInfo: UserPageInfo,
    userTopics: LazyPagingItems<UserTopics.Item>,
    userReplies: LazyPagingItems<UserReplies.Item>,
    topicTitleOverview: Boolean,
    isLoggedIn: Boolean,
    topicsListState: LazyListState,
    repliesListState: LazyListState,
    selectedTabIndex: Int,
    headerAlpha: Float,
    sizedHtmls: SnapshotStateMap<String, String>,
    onSelectedTabIndexChange: (Int) -> Unit,
    onHeaderMeasured: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    val tabNames =
        listOf(stringResource(Res.string.user_topic), stringResource(Res.string.user_reply))

    when (selectedTabIndex) {
        0 -> UserTopicsList(
            items = userTopics,
            userPageInfo = userPageInfo,
            isLoggedIn = isLoggedIn,
            topicTitleOverview = topicTitleOverview,
            lazyListState = topicsListState,
            headerAlpha = headerAlpha,
            selectedTabIndex = selectedTabIndex,
            tabNames = tabNames,
            onTabSelected = onSelectedTabIndexChange,
            onHeaderMeasured = onHeaderMeasured,
            onFollowClick = onFollowClick,
            onBlockClick = onBlockClick,
            onTopicClick = onTopicClick,
            onNodeClick = onNodeClick,
        )

        else -> UserRepliesList(
            items = userReplies,
            userPageInfo = userPageInfo,
            isLoggedIn = isLoggedIn,
            sizedHtmls = sizedHtmls,
            lazyListState = repliesListState,
            headerAlpha = headerAlpha,
            selectedTabIndex = selectedTabIndex,
            tabNames = tabNames,
            onTabSelected = onSelectedTabIndexChange,
            onHeaderMeasured = onHeaderMeasured,
            onFollowClick = onFollowClick,
            onBlockClick = onBlockClick,
            onTopicClick = onTopicClick,
            openUri = openUri,
            loadHtmlImage = loadHtmlImage,
            onHtmlImageClick = onHtmlImageClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserTopicsList(
    items: LazyPagingItems<UserTopics.Item>,
    userPageInfo: UserPageInfo,
    isLoggedIn: Boolean,
    topicTitleOverview: Boolean,
    lazyListState: LazyListState,
    headerAlpha: Float,
    selectedTabIndex: Int,
    tabNames: List<String>,
    onTabSelected: (Int) -> Unit,
    onHeaderMeasured: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
        item(key = "user_header") {
            UserHeaderSection(
                userPageInfo = userPageInfo,
                isLoggedIn = isLoggedIn,
                alpha = headerAlpha,
                onMeasured = onHeaderMeasured,
                onFollowClick = onFollowClick,
                onBlockClick = onBlockClick,
            )
        }
        stickyHeader(key = "user_tabs") {
            UserTabs(
                selectedTabIndex = selectedTabIndex,
                tabNames = tabNames,
                onTabSelected = onTabSelected,
            )
        }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTopicClick(topic.link) }) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text(
                    topic.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                    maxLines = if (topicTitleOverview) Constants.topicTitleOverviewMaxLines else Int.MAX_VALUE,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserRepliesList(
    items: LazyPagingItems<UserReplies.Item>,
    userPageInfo: UserPageInfo,
    isLoggedIn: Boolean,
    sizedHtmls: SnapshotStateMap<String, String>,
    lazyListState: LazyListState,
    headerAlpha: Float,
    selectedTabIndex: Int,
    tabNames: List<String>,
    onTabSelected: (Int) -> Unit,
    onHeaderMeasured: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {
        item(key = "user_header") {
            UserHeaderSection(
                userPageInfo = userPageInfo,
                isLoggedIn = isLoggedIn,
                alpha = headerAlpha,
                onMeasured = onHeaderMeasured,
                onFollowClick = onFollowClick,
                onBlockClick = onBlockClick,
            )
        }
        stickyHeader(key = "user_tabs") {
            UserTabs(
                selectedTabIndex = selectedTabIndex,
                tabNames = tabNames,
                onTabSelected = onTabSelected,
            )
        }
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
private fun UserHeaderSection(
    userPageInfo: UserPageInfo,
    isLoggedIn: Boolean,
    alpha: Float,
    onMeasured: (Int) -> Unit,
    onFollowClick: () -> Unit,
    onBlockClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { onMeasured(it.height) }
            .graphicsLayer(alpha = alpha)
    ) {
        UserHeader(
            userPageInfo = userPageInfo,
            isLoggedIn = isLoggedIn,
            onFollowClick = onFollowClick,
            onBlockClick = onBlockClick,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
        ListDivider()
    }
}

@Composable
private fun UserTabs(
    selectedTabIndex: Int,
    tabNames: List<String>,
    onTabSelected: (Int) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions: List<TabPosition> ->
                UserTabIndicator(tabPosition = tabPositions[selectedTabIndex])
            }
        ) {
            tabNames.forEachIndexed { index, name ->
                val selected = selectedTabIndex == index
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = name,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                }
            }
        }
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
    Box(
        modifier = Modifier
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

private fun calculateHeaderAlpha(
    listState: LazyListState,
    headerHeightPx: Int,
): Float {
    if (listState.firstVisibleItemIndex > 0) {
        return 0f
    }
    if (headerHeightPx <= 0) {
        return 1f
    }
    val progress = listState.firstVisibleItemScrollOffset.toFloat() / headerHeightPx.toFloat()
    return (1f - progress).coerceIn(0f, 1f)
}
