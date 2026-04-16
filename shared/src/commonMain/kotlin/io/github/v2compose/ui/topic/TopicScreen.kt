package io.github.v2compose.ui.topic

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.v2compose.LocalAppPlatformHandlers
import io.github.v2compose.V2exUri
import io.github.v2compose.core.plainTextClipEntry
import io.github.v2compose.network.bean.TopicInfo
import io.github.v2compose.network.bean.TopicInfo.ContentInfo.Supplement
import io.github.v2compose.network.bean.TopicInfo.Reply
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.HtmlAlertDialog
import io.github.v2compose.ui.common.HtmlContent
import io.github.v2compose.ui.common.ListDivider
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.github.v2compose.ui.common.PlatformBackHandler
import io.github.v2compose.ui.common.SimpleTopic
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingPrependMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import io.github.v2compose.ui.gallery.composables.PopupImage
import io.github.v2compose.ui.topic.bean.ReplyWrapper
import io.github.v2compose.ui.topic.bean.TopicInfoWrapper
import io.github.v2compose.ui.topic.composables.ReplyInput
import io.github.v2compose.ui.topic.composables.ReplyInputState
import io.github.v2compose.ui.topic.composables.ReplyMenuItem
import io.github.v2compose.ui.topic.composables.TopicMenuItem
import io.github.v2compose.ui.topic.composables.TopicReply
import io.github.v2compose.ui.topic.composables.TopicTopBar
import io.github.v2compose.ui.topic.composables.UserRepliesDialog
import io.github.v2compose.ui.topic.composables.fabSizeWithMargin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import io.github.v2compose.util.KLogger
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.n_comment
import v2compose.shared.generated.resources.replies_order_negative
import v2compose.shared.generated.resources.replies_order_positive

private const val TAG = "TopicScreen"

@Composable
fun TopicScreenRoute(
    onBackClick: () -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onAddSupplementClick: (String) -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    onShareTopic: (String, String) -> Unit,
    viewModel: TopicViewModel = koinViewModel(),
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val platformHandlers = LocalAppPlatformHandlers.current
    val args = viewModel.topicArgs
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val repliesReversed by viewModel.repliesReversed.collectAsStateWithLifecycle(initialValue = true)
    val highlightOpReply by viewModel.highlightOpReply.collectAsStateWithLifecycle()
    val replyWithFloor by viewModel.replyWithFloor.collectAsStateWithLifecycle()
    val topicItems = viewModel.topicItems.collectAsLazyPagingItems()

    val pagingTopicInfo = if (topicItems.itemCount > 0) {
        topicItems.itemSnapshotList.firstOrNull { it is TopicInfo } as? TopicInfo
    } else null
    LaunchedEffect(pagingTopicInfo) {
        pagingTopicInfo?.let { viewModel.updateTopicInfoWrapper(topic = it) }
    }

    val topicInfoWrapper by viewModel.topicInfoWrapper
    val resolvedTopicInfoWrapper = pagingTopicInfo?.let { topicInfoWrapper.copy(topic = it) }
        ?: topicInfoWrapper
    val replyWrappers = viewModel.replyWrappers
    val replyTopicState by viewModel.replyTopicState.collectAsStateWithLifecycle()
    var htmlImageUrl by rememberSaveable { mutableStateOf("") }

    if (htmlImageUrl.isNotEmpty()) {
        PopupImage(imageUrl = htmlImageUrl) {
            htmlImageUrl = ""
        }
    }

    KLogger.d(TAG, "topic args = $args")

    HandleReplyTopicState(replyTopicState, topicItems, openUri)

    HandleSnackbarMessage(viewModel)

    TopicScreen(
        targetFloor = args.replyFloor,
        isLoggedIn = isLoggedIn,
        topicInfo = resolvedTopicInfoWrapper,
        repliesOrder = if (repliesReversed) RepliesOrder.Negative else RepliesOrder.Positive,
        topicItems = topicItems,
        sizedHtmls = viewModel.sizedHtmls,
        replyWrappers = replyWrappers,
        replyTopicState = replyTopicState,
        highlightOpReply = highlightOpReply,
        replyWithFloor = replyWithFloor,
        onBackClick = onBackClick,
        onTopicMenuClick = {
            when (it) {
                TopicMenuItem.Favorite -> viewModel.favoriteTopic()
                TopicMenuItem.Favorited -> viewModel.unFavoriteTopic()
                TopicMenuItem.Append -> onAddSupplementClick(args.topicId)
                TopicMenuItem.Thanks -> viewModel.thanksTopic()
                TopicMenuItem.Thanked -> viewModel.unThanksTopic()
                TopicMenuItem.Ignore -> viewModel.ignoreTopic()
                TopicMenuItem.Ignored -> viewModel.unIgnoreTopic()
                TopicMenuItem.Report -> viewModel.reportTopic()
                TopicMenuItem.Reported -> viewModel.unReportTopic()
                TopicMenuItem.Share -> {
                    pagingTopicInfo?.headerInfo?.let { header ->
                        onShareTopic(header.title, V2exUri.topicUrl(args.topicId))
                    }
                }
                TopicMenuItem.OpenInBrowser ->
                    platformHandlers.openExternalUri(V2exUri.topicUrl(args.topicId))
                TopicMenuItem.More -> Unit
            }
        },
        onUserAvatarClick = onUserAvatarClick,
        onNodeClick = onNodeClick,
        onRepliedOrderClick = { viewModel.toggleRepliesReversed() },
        openUri = openUri,
        onReplyMenuItemClick = { menuItem, reply ->
            when (menuItem) {
                ReplyMenuItem.Thank -> viewModel.thankReply(reply)
                ReplyMenuItem.Ignore -> viewModel.ignoreReply(reply)
                ReplyMenuItem.Copy -> {
                    coroutineScope.launch {
                        clipboard.setClipEntry(plainTextClipEntry(reply.replyContent))
                        viewModel.notifyReplyCopied()
                    }
                }
                ReplyMenuItem.HomePage -> onUserAvatarClick(reply.userName, reply.avatar)
                else -> {}
            }
        },
        loadHtmlImage = viewModel::loadHtmlImage,
        onSendComment = viewModel::replyTopic,
        onHtmlImageClick = { current, _ -> htmlImageUrl = current },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopicScreen(
    targetFloor: Int,
    isLoggedIn: Boolean,
    topicInfo: TopicInfoWrapper,
    repliesOrder: RepliesOrder,
    topicItems: LazyPagingItems<Any>,
    sizedHtmls: SnapshotStateMap<String, String>,
    replyWrappers: Map<String, ReplyWrapper>,
    replyTopicState: ReplyTopicState,
    highlightOpReply: Boolean,
    replyWithFloor: Boolean,
    onBackClick: () -> Unit,
    onTopicMenuClick: (TopicMenuItem) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onRepliedOrderClick: (RepliesOrder) -> Unit,
    openUri: (String) -> Unit,
    onReplyMenuItemClick: (ReplyMenuItem, Reply) -> Unit,
    loadHtmlImage: (String, String, String?) -> Unit,
    onSendComment: (String) -> Unit,
    onHtmlImageClick: (String, List<String>) -> Unit,
) {
    val density = LocalDensity.current

    var clickReplyTimes by remember { mutableIntStateOf(0) }
    var replyInputInitialText by remember { mutableStateOf("") }
    var replyInputCurrentText by remember { mutableStateOf("") }
    var replyInputState by remember { mutableStateOf(ReplyInputState.Collapsed) }

    val scrollState = topicItems.rememberLazyListState()
    val topBarShowTopicTitle by remember(density, scrollState) {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset < with(
                density
            ) { -64.dp.toPx() }
        }
    }
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    PlatformBackHandler(enabled = replyInputState == ReplyInputState.Expanded) {
        replyInputState = ReplyInputState.Collapsed
    }

    Scaffold(
        topBar = {
            TopicTopBar(
                isLoggedIn = isLoggedIn,
                topicInfo = topicInfo,
                showTopicTitle = topBarShowTopicTitle,
                onBackClick = onBackClick,
                onMenuClick = onTopicMenuClick,
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            val fabType = if (replyTopicState == ReplyTopicState.Loading) FabType.Loading else {
                when (replyInputState) {
                    ReplyInputState.Collapsed -> FabType.Reply
                    ReplyInputState.Expanded -> FabType.Send
                }
            }
            FabButton(visible = isLoggedIn, type = fabType, onClick = { tabType ->
                if (fabType == FabType.Send) {
                    onSendComment(replyInputCurrentText)
                    replyInputState = ReplyInputState.Collapsed
                } else if (tabType == FabType.Reply) {
                    replyInputState = ReplyInputState.Expanded
                }
            })
        },
        contentWindowInsets = WindowInsets.ime.union(WindowInsets.systemBars),
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            TopicList(
                topicInfo = topicInfo,
                repliesOrder = repliesOrder,
                targetFloor = targetFloor,
                topicItems = topicItems,
                lazyListState = scrollState,
                sizedHtmls = sizedHtmls,
                replyWrappers = replyWrappers,
                isLoggedIn = isLoggedIn,
                highlightOpReply = highlightOpReply,
                onUserAvatarClick = onUserAvatarClick,
                onNodeClick = onNodeClick,
                onRepliedOrderClick = onRepliedOrderClick,
                onTopicReplyClick = {
                    replyInputInitialText = initialReplyText(it, replyWithFloor)
                    replyInputState = ReplyInputState.Expanded
                    clickReplyTimes++
                },
                openUri = openUri,
                onTopicMenuItemClick = { menuItem, reply ->
                    if (menuItem == ReplyMenuItem.Reply) {
                        replyInputInitialText = initialReplyText(reply, replyWithFloor)
                        replyInputState = ReplyInputState.Expanded
                    } else {
                        onReplyMenuItemClick(menuItem, reply)
                    }
                },
                loadHtmlImage = loadHtmlImage,
                onHtmlImageClick = onHtmlImageClick,
                modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            )

            ReplyInput(
                initialValue = replyInputInitialText,
                clickReplyTimes = clickReplyTimes,
                onValueChanged = { replyInputCurrentText = it },
                state = replyInputState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            if (replyTopicState is ReplyTopicState.Success) {
                LaunchedEffect(true) {
                    replyInputInitialText = ""
                }
            }
        }
    }
}

fun initialReplyText(mention: Reply?, replyWithFloor: Boolean): String {
    if (mention == null) return ""
    var text = "@${mention.userName} "
    if (replyWithFloor) {
        text += "#${mention.floor} "
    }
    return text
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopicList(
    topicInfo: TopicInfoWrapper,
    repliesOrder: RepliesOrder,
    targetFloor: Int,
    topicItems: LazyPagingItems<Any>,
    lazyListState: LazyListState,
    sizedHtmls: SnapshotStateMap<String, String>,
    replyWrappers: Map<String, ReplyWrapper>,
    isLoggedIn: Boolean,
    highlightOpReply: Boolean,
    onUserAvatarClick: (String, String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onRepliedOrderClick: (RepliesOrder) -> Unit,
    onTopicReplyClick: (Reply) -> Unit,
    openUri: (String) -> Unit,
    onTopicMenuItemClick: (ReplyMenuItem, Reply) -> Unit,
    loadHtmlImage: (tag: String, html: String, img: String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val clickedUserReplies = remember { mutableStateListOf<List<Reply>>() }
    val repliesBarIndex = rememberRepliesBarIndex(topicInfo)
    var currentTargetFloor by rememberSaveable { mutableIntStateOf(targetFloor) }

    val repliesBarHeight = with(LocalDensity.current) { 40.dp.roundToPx() }
    val floorRegex = "#reply\\d+".toRegex()
    val hasShakeTargetFloor by rememberSaveable(currentTargetFloor) { mutableStateOf(false) }

    fun clickUriHandler(uri: String, reply: Reply) {
        while (true) {
            if (!uri.startsWith("/member/")) break
            val userName = uri.removePrefix("/member/")
            val userReplies =
                topicItems.itemSnapshotList.filterIsInstance<Reply>()
                    .filter { it.floor < reply.floor && it.userName == userName }
            if (userReplies.isEmpty()) break
            clickedUserReplies.add(userReplies)
            return
        }
        if (floorRegex.matches(uri)) {
            KLogger.d(TAG, "clickUriHandler, uri = $uri")
            val floor = uri.substring("#reply".length).toIntOrNull() ?: return
            val floorReply =
                topicItems.itemSnapshotList.firstOrNull { it is Reply && it.floor == floor } as Reply?
                    ?: return
            clickedUserReplies.add(listOf(floorReply))
            return
        }
        openUri(uri)
    }

    clickedUserReplies.forEachIndexed { index, item ->
        UserRepliesDialog(
            userReplies = item,
            sizedHtmls = sizedHtmls,
            onDismissRequest = { clickedUserReplies.removeAt(index) },
            onUserAvatarClick = onUserAvatarClick,
            onUriClick = { uri, reply -> clickUriHandler(uri, reply) },
            loadHtmlImage = loadHtmlImage,
            onHtmlImageClick = onHtmlImageClick,
        )
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(bottom = if (isLoggedIn) fabSizeWithMargin else 0.dp),
    ) {
        pagingRefreshItem(topicItems)

        if (topicInfo.topic != null) {
            if (!topicInfo.topic.isValid()) {
                //TODO 非登录状态，触发某些关键字（如 fg ），重定向到首页，导致解析失败
                return@LazyColumn
            }

            item(key = "title", contentType = "title") {
                TopicTitle(
                    topicInfo = topicInfo.topic,
                    onUserAvatarClick = onUserAvatarClick,
                    onNodeClick = onNodeClick
                )
            }

            val contentInfo = topicInfo.topic.contentInfo ?: return@LazyColumn
            if (contentInfo.content.isNotEmpty()) {
                val tag = "content"
                item(key = tag, contentType = "content") {
                    val content = contentInfo.content
                    TopicContent(
                        content = sizedHtmls[tag] ?: content,
                        openUri = openUri,
                        loadHtmlImage = { html, src -> loadHtmlImage(tag, html, src) },
                        onHtmlImageClick = onHtmlImageClick,
                    )
                }
            }

            if (contentInfo.supplements.isNotEmpty()) {
                val supplements = contentInfo.supplements
                itemsIndexed(
                    items = supplements,
                    key = { supplementIndex, _ -> "supplement:$supplementIndex" },
                    contentType = { _, _ -> "supplement" }) { supplementIndex, item ->
                    val tag = "supplement:$supplementIndex"
                    TopicSupplement(
                        index = supplementIndex,
                        supplement = item,
                        content = sizedHtmls[tag] ?: item.content,
                        openUri = { uri ->
                            if (uri.startsWith("#reply")) {
                                uri.substring("#reply".length).toIntOrNull()?.let {
                                    currentTargetFloor = it
                                }
                            } else {
                                openUri(uri)
                            }
                        },
                        loadHtmlImage = { html, src -> loadHtmlImage(tag, html, src) },
                        onHtmlImageClick = onHtmlImageClick,
                    )
                }
            }

            if (contentInfo.content.isNotEmpty() && contentInfo.supplements.isEmpty()) {
                item(key = "divider#onRepliesBar", contentType = "divider") {
                    ListDivider(
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            }

            stickyHeader(key = "repliesBar", contentType = "repliesBar") {
                TopicRepliesBar(
                    replyNum = topicInfo.topic.headerInfo!!.getCommentNum(),
                    repliesOrder = repliesOrder,
                    onRepliedOrderClick = {
                        onRepliedOrderClick(it)
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(repliesBarIndex)
                        }
                    },
                )
            }
        }

        pagingPrependMoreItem(lazyPagingItems = topicItems)

        items(
            topicItems.itemCount,
            key = { index -> topicItems[index].let { if (it is Reply) it.replyId() else "item#$index" } }) { index ->
            val item = topicItems[index] ?: return@items
            if (item is Reply) {
                val replyWrapper = replyWrappers[item.replyId()]
                if (replyWrapper?.ignored == true) {
                    return@items
                }
                val tag = "reply#${item.replyId()}"
                TopicReply(
                    index = index,
                    reply = item,
                    replyWrapper = replyWrapper,
                    opName = topicInfo.topic?.headerInfo?.userName ?: "",
                    isLoggedIn = isLoggedIn,
                    content = sizedHtmls[tag] ?: item.replyContent,
                    highlightOpReply = highlightOpReply,
                    shakeable = item.floor == currentTargetFloor && !hasShakeTargetFloor,
                    onShakeFinished = { currentTargetFloor = -1 },
                    onUserAvatarClick = onUserAvatarClick,
                    onUriClick = { uri, reply -> clickUriHandler(uri, reply) },
                    onClick = {
                        onTopicReplyClick(it)
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(
                                repliesBarIndex + index + 1, -repliesBarHeight
                            )
                            delay(400)
                            lazyListState.animateScrollToItem(
                                repliesBarIndex + index + 1, -repliesBarHeight
                            )
                        }
                    },
                    onMenuItemClick = { onTopicMenuItemClick(it, item) },
                    loadHtmlImage = { html, src -> loadHtmlImage(tag, html, src) },
                    onHtmlImageClick = onHtmlImageClick,
                )

            }
        }
        pagingAppendMoreItem(lazyPagingItems = topicItems)
    }

    val targetFloorIndex = remember(topicItems.itemSnapshotList, currentTargetFloor) {
        topicItems.itemSnapshotList.indexOfFirst { it is Reply && it.floor == currentTargetFloor }
    }
    if (repliesBarIndex >= 0 && targetFloorIndex >= 0) {
        LaunchedEffect(repliesBarIndex, targetFloorIndex) {
            lazyListState.animateScrollToItem(
                repliesBarIndex + targetFloorIndex + 1, -repliesBarHeight
            )
        }
    }
}

@Composable
fun rememberRepliesBarIndex(topicInfo: TopicInfoWrapper): Int {
    return remember(topicInfo) {
        if (topicInfo.topic == null || !topicInfo.topic.isValid()) {
            -1
        } else {
            val contentInfo = topicInfo.topic.contentInfo ?: return@remember -1
            var index = 0 //title
            if (contentInfo.content.isNotEmpty()) {
                index++ //content
            }
            if (contentInfo.supplements.isNotEmpty()) {
                index += contentInfo.supplements.size //supplements
            }
            if (contentInfo.content.isNotEmpty() && contentInfo.supplements.isEmpty()) {
                index++ //divider
            }
            ++index//repliesBar
        }
    }

}

@Composable
private fun TopicTitle(
    topicInfo: TopicInfo?,
    onUserAvatarClick: (String, String) -> Unit,
    onNodeClick: (String, String) -> Unit
) {
    val headerInfo = topicInfo?.headerInfo ?: return
    SimpleTopic(
        userName = headerInfo.userName,
        userAvatar = headerInfo.avatar,
        time = headerInfo.getTime(),
        replyCount = headerInfo.getCommentNum(),
        viewCount = headerInfo.getViewCount(),
        nodeName = headerInfo.getTagName(),
        nodeTitle = headerInfo.tag,
        title = headerInfo.title,
        onUserAvatarClick = {
            onUserAvatarClick(
                headerInfo.userName, headerInfo.avatar
            )
        },
        onNodeClick = {
            onNodeClick(headerInfo.getTagName(), headerInfo.tag)
        })
}

@Composable
private fun TopicContent(
    content: String,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    HtmlContent(
        content = content,
        sourceContent = content,
        selectable = false,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        onUriClick = openUri,
        loadImage = loadHtmlImage,
        onHtmlImageClick = onHtmlImageClick,
    )
}

@Composable
private fun TopicSupplement(
    index: Int,
    supplement: Supplement,
    content: String,
    openUri: (String) -> Unit,
    loadHtmlImage: (String, String?) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val leftBorderColor = MaterialTheme.colorScheme.tertiary

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        ListDivider(modifier = Modifier.align(alignment = Alignment.BottomCenter))
        Column(
            modifier = Modifier
                .drawBehind {
                    drawRect(color = backgroundColor)
                    drawRect(color = leftBorderColor, size = size.copy(width = 4.dp.toPx()))
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                supplement.title,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                style = MaterialTheme.typography.labelMedium
            )
            HtmlContent(
                content = content,
                sourceContent = supplement.content,
                selectable = false,
                modifier = Modifier.fillMaxWidth(),
                onUriClick = openUri,
                loadImage = loadHtmlImage,
                onHtmlImageClick = onHtmlImageClick,
            )
        }
    }
}

enum class RepliesOrder(val label: StringResource) {
    //最新的回复排在最前面
    Negative(Res.string.replies_order_negative),

    //最早的回复排在最前面
    Positive(Res.string.replies_order_positive),
}

@Composable
private fun TopicRepliesBar(
    replyNum: String, repliesOrder: RepliesOrder, onRepliedOrderClick: (RepliesOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(Res.string.n_comment, replyNum),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = CircleShape,
                )
                .padding(2.dp),
        ) {
            RepliesOrder.entries.forEach { order ->
                val selected = repliesOrder == order
                Text(
                    text = stringResource(order.label),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                            },
                            shape = CircleShape,
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { onRepliedOrderClick(order) },
                )
            }
        }
    }
}


enum class FabType {
    Reply, Send, Loading
}

@Composable
private fun FabButton(
    visible: Boolean,
    type: FabType,
    onClick: (FabType) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    if (visible) {
        FloatingActionButton(
            shape = CircleShape,
            onClick = { onClick(type) },
            modifier = Modifier.focusRequester(focusRequester)
        ) {
            val contentColor = LocalContentColor.current
            AnimatedContent(targetState = type) { state ->
                when (state) {
                    FabType.Send -> Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        type.name,
                        tint = contentColor
                    )

                    FabType.Reply -> Icon(
                        Icons.AutoMirrored.Rounded.Comment,
                        type.name,
                        tint = contentColor
                    )

                    FabType.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun HandleReplyTopicState(
    replyTopicState: ReplyTopicState,
    topicItems: LazyPagingItems<Any>,
    onUriClick: (String) -> Unit,
) {
    if (replyTopicState is ReplyTopicState.Success) {
        LaunchedEffect(replyTopicState) {
            topicItems.refresh()
        }
    } else if (replyTopicState is ReplyTopicState.Failure) {
        val problem = rememberSaveable(replyTopicState) { replyTopicState.result.problem }
        HtmlAlertDialog(content = problem, onUriClick = onUriClick)
    }
}
