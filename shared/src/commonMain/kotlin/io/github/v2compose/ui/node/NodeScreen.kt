package io.github.v2compose.ui.node

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import io.github.v2compose.Constants
import io.github.v2compose.core.extension.castOrNull
import io.github.v2compose.network.bean.NodeInfo
import io.github.v2compose.network.bean.NodeTopicInfo
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.BackIcon
import io.github.v2compose.ui.common.HtmlContent
import io.github.v2compose.ui.common.ListDivider
import io.github.v2compose.ui.common.LoadError
import io.github.v2compose.ui.common.Loading
import io.github.v2compose.ui.common.TextAlertDialog
import io.github.v2compose.ui.common.TopicUserAvatar
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import io.github.v2compose.util.KLogger
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.n_comment
import v2compose.shared.generated.resources.node
import v2compose.shared.generated.resources.node_click_times
import v2compose.shared.generated.resources.node_favorite
import v2compose.shared.generated.resources.node_favorited
import v2compose.shared.generated.resources.node_topics_and_favorites
import v2compose.shared.generated.resources.node_unfavorite_tips

private const val TAG = "NodeScreen"

@Composable
fun NodeRoute(
    onBackClick: () -> Unit,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    onShareNode: (String, String) -> Unit,
    viewModel: NodeViewModel = koinViewModel(),
) {
    val nodeArgs = viewModel.nodeArgs
    val nodeUiState by viewModel.nodeInfo.collectAsStateWithLifecycle()
    val nodeTopicItems = viewModel.nodeTopicItems.collectAsLazyPagingItems()
    val topicTitleOverview by viewModel.topicTitleOverview.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val pagingNodeTopicInfo = if (nodeTopicItems.itemCount > 0) {
        nodeTopicItems.peek(0).castOrNull<NodeTopicInfo>()
    } else null

    LaunchedEffect(pagingNodeTopicInfo) {
        viewModel.updateNodeTopicInfo(pagingNodeTopicInfo)
    }
    val nodeTopicInfo by viewModel.nodeTopicInfo.collectAsStateWithLifecycle()

    HandleSnackbarMessage(viewModel)

    NodeScreen(
        nodeArgs = nodeArgs,
        nodeUiState = nodeUiState,
        nodeTopicInfo = nodeTopicInfo,
        nodeTopicItems = nodeTopicItems,
        topicTitleOverview = topicTitleOverview,
        isLoggedIn = isLoggedIn,
        onBackClick = onBackClick,
        onFavoriteClick = viewModel::follow,
        onRetryNodeClick = viewModel::retryNode,
        onTopicClick = onTopicClick,
        onUserAvatarClick = onUserAvatarClick,
        onShareClick = {
            val title = (nodeUiState as? NodeUiState.Success)?.let {
                "V2EX > ${it.nodeInfo.name}\n${it.nodeInfo.title}"
            } ?: "V2EX > ${nodeArgs.nodeTitle.orEmpty()}"
            onShareNode(title, "https://www.v2ex.com/go/${nodeArgs.nodeName}")
        },
        openUri = openUri,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeScreen(
    nodeArgs: NodeArgs,
    nodeUiState: NodeUiState,
    nodeTopicInfo: NodeTopicInfo?,
    nodeTopicItems: LazyPagingItems<Any>,
    topicTitleOverview: Boolean,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRetryNodeClick: () -> Unit,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onShareClick: () -> Unit,
    openUri: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                NodeTopBarTitle(
                    scrollBehavior = scrollBehavior,
                    nodeArgs = nodeArgs,
                    nodeUiState = nodeUiState,
                    nodeTopicInfo = nodeTopicInfo,
                    isLoggedIn = isLoggedIn,
                    onBackClick = onBackClick,
                    onFavoriteClick = onFavoriteClick,
                    onShareClick = onShareClick,
                )
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                NodeContent(
                    nodeUiState = nodeUiState,
                    lazyPagingItems = nodeTopicItems,
                    topicTitleOverview = topicTitleOverview,
                    onTopicClick = onTopicClick,
                    onUserAvatarClick = onUserAvatarClick,
                    onRetryNodeClick = onRetryNodeClick,
                    openUri = openUri,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NodeTopBarTitle(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    nodeArgs: NodeArgs,
    nodeUiState: NodeUiState,
    nodeTopicInfo: NodeTopicInfo?,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    var favorited by remember(nodeTopicInfo) { mutableStateOf(nodeTopicInfo?.hasStared) }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    val onFavoriteClickInternal = {
        nodeTopicInfo?.let {
            val stared = it.hasStared
            if (favorited == stared) {
                if (stared) {
                    showUnfollowDialog = true
                } else {
                    favorited = true
                    onFavoriteClick()
                }
            }
        }
    }

    if (showUnfollowDialog) {
        TextAlertDialog(
            message = stringResource(Res.string.node_unfavorite_tips),
            onConfirm = {
                favorited = false
                onFavoriteClick()
            },
            onDismiss = { showUnfollowDialog = false },
        )
    }

    LargeTopAppBar(
        title = {
            val progress = scrollBehavior.state.collapsedFraction
            NodeTopBarTitle(
                nodeArgs,
                nodeUiState,
                isLoggedIn,
                favorited,
                progress,
                onFavoriteClickInternal
            )
        },
        navigationIcon = { BackIcon(onBackClick = onBackClick) },
        actions = {
            val progress = scrollBehavior.state.collapsedFraction
            if (isLoggedIn) {
                favorited?.let {
                    IconButton(
                        onClick = { onFavoriteClickInternal() },
                        modifier = Modifier.graphicsLayer(alpha = progress)
                    ) {
                        Icon(
                            if (it) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                            "favorite"
                        )
                    }
                }
            }

            IconButton(onClick = onShareClick) {
                Icon(Icons.Rounded.Share, "share node")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun NodeTopBarTitle(
    nodeArgs: NodeArgs,
    nodeUiState: NodeUiState,
    isLoggedIn: Boolean,
    favorited: Boolean?,
    progress: Float,
    onFavoriteClickInternal: () -> Unit?
) {
    Box {
        NodeTitle(
            nodeArgs = nodeArgs,
            nodeUiState = nodeUiState,
        )

        if (isLoggedIn) {
            favorited?.let {
                val contentColor =
                    if (it) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface

                AssistChip(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .graphicsLayer(
                            alpha = 1f - progress,
                            translationY = progress * 100f
                        ),
                    onClick = { onFavoriteClickInternal() },
                    leadingIcon = {
                        Icon(
                            if (it) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                            "favorite",
                        )
                    },
                    label = {
                        Text(
                            stringResource(if (it) Res.string.node_favorited else Res.string.node_favorite),
                            color = contentColor
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }
    }
}

@Composable
private fun NodeTitle(
    nodeArgs: NodeArgs,
    nodeUiState: NodeUiState,
    modifier: Modifier = Modifier,
) {
    val nodeInfo =
        remember(nodeUiState) { if (nodeUiState is NodeUiState.Success) nodeUiState.nodeInfo else null }
    val nodeTitle = nodeInfo?.title ?: nodeArgs.nodeTitle
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AsyncImage(
            model = nodeInfo?.avatar,
            contentDescription = nodeTitle,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            Text(
                nodeTitle ?: stringResource(Res.string.node),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
            )
            nodeInfo?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.node_topics_and_favorites, it.topics, it.stars),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NodeContent(
    nodeUiState: NodeUiState,
    lazyPagingItems: LazyPagingItems<Any>,
    topicTitleOverview: Boolean,
    onRetryNodeClick: () -> Unit,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (nodeUiState) {
        is NodeUiState.Success -> {
            TopicList(
                nodeInfo = nodeUiState.nodeInfo,
                lazyPagingItems = lazyPagingItems,
                topicTitleOverview = topicTitleOverview,
                onTopicClick = onTopicClick,
                onUserAvatarClick = onUserAvatarClick,
                openUri = openUri,
                modifier = modifier,
            )
        }

        is NodeUiState.Error -> {
            LoadError(
                error = nodeUiState.error,
                onRetryClick = onRetryNodeClick,
                modifier = modifier
            )
        }

        is NodeUiState.Loading -> {
            Loading(modifier = modifier)
        }
    }
}

@Composable
private fun TopicList(
    nodeInfo: NodeInfo,
    lazyPagingItems: LazyPagingItems<Any>,
    topicTitleOverview: Boolean,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nodeTopicInfo: NodeTopicInfo? = if (lazyPagingItems.itemCount > 0) {
        lazyPagingItems.peek(0).castOrNull<NodeTopicInfo>()
    } else null

    if (nodeTopicInfo != null && !nodeTopicInfo.isValid()) {
        KLogger.e(TAG, "node topic info is invalid, nodeInfo = $nodeInfo")
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize(), state = lazyPagingItems.rememberLazyListState()) {
        pagingRefreshItem(lazyPagingItems = lazyPagingItems)
        items(lazyPagingItems.itemCount, lazyPagingItems.itemKey()) { index ->
            val item = lazyPagingItems[index]
            if (item is NodeTopicInfo) {
                if (nodeInfo.header.isNotEmpty()) {
                    NodeDescription(desc = nodeInfo.header, openUri = openUri)
                }
            } else if (item is NodeTopicInfo.Item) {
                NodeTopic(
                    item = item,
                    titleOverview = topicTitleOverview,
                    onTopicClick = onTopicClick,
                    onUserAvatarClick = onUserAvatarClick
                )
            }
        }
        pagingAppendMoreItem(lazyPagingItems = lazyPagingItems)
    }
}

@Composable
private fun NodeDescription(
    desc: String,
    openUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        HtmlContent(
            content = desc,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            onUriClick = openUri,
        )
        ListDivider(modifier = Modifier.align(alignment = Alignment.BottomCenter))
    }
}

@Composable
private fun NodeTopic(
    item: NodeTopicInfo.Item,
    titleOverview: Boolean,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit
) {
    Box(modifier = Modifier.clickable { onTopicClick(item) }) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TopicUserAvatar(
                    userName = item.userName,
                    userAvatar = item.avatar,
                    onUserAvatarClick = { onUserAvatarClick(item.userName, item.avatar) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.userName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.FirstLineTop,
                            )
                        )
                    )

                    Row {
                        Text(
                            stringResource(Res.string.node_click_times, item.clickNum),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(Res.string.n_comment, item.commentNum),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (titleOverview) Constants.topicTitleOverviewMaxLines else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ListDivider(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
