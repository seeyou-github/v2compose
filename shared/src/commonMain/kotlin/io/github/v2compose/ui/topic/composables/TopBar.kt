package io.github.v2compose.ui.topic.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.v2compose.ui.common.BackIcon
import io.github.v2compose.ui.topic.bean.TopicInfoWrapper
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.menu_item_thank
import v2compose.shared.generated.resources.menu_item_unthank
import v2compose.shared.generated.resources.topic
import v2compose.shared.generated.resources.topic_menu_item_append
import v2compose.shared.generated.resources.topic_menu_item_favorite
import v2compose.shared.generated.resources.topic_menu_item_ignore
import v2compose.shared.generated.resources.topic_menu_item_more
import v2compose.shared.generated.resources.topic_menu_item_open_in_browser
import v2compose.shared.generated.resources.topic_menu_item_report
import v2compose.shared.generated.resources.topic_menu_item_reported
import v2compose.shared.generated.resources.topic_menu_item_share
import v2compose.shared.generated.resources.topic_menu_item_text_size
import v2compose.shared.generated.resources.topic_menu_item_unfavorite
import v2compose.shared.generated.resources.topic_menu_item_unignore


enum class TopicMenuItem(val icon: ImageVector, val label: StringResource) {
    Append(Icons.AutoMirrored.Rounded.NoteAdd, Res.string.topic_menu_item_append),
    Favorite(Icons.Rounded.BookmarkAdd, Res.string.topic_menu_item_favorite),
    Favorited(Icons.Rounded.BookmarkAdded, Res.string.topic_menu_item_unfavorite),
    More(Icons.Rounded.MoreVert, Res.string.topic_menu_item_more),
    Thanks(Icons.Rounded.FavoriteBorder, Res.string.menu_item_thank),
    Thanked(Icons.Rounded.Favorite, Res.string.menu_item_unthank),
    Ignore(Icons.Rounded.VisibilityOff, Res.string.topic_menu_item_ignore),
    Ignored(Icons.Rounded.Visibility, Res.string.topic_menu_item_unignore),
    Report(Icons.Rounded.Report, Res.string.topic_menu_item_report),
    Reported(Icons.Outlined.Report, Res.string.topic_menu_item_reported),
    Share(Icons.Rounded.Share, Res.string.topic_menu_item_share),
    OpenInBrowser(Icons.Rounded.OpenInBrowser, Res.string.topic_menu_item_open_in_browser),
    TextSize(Icons.Rounded.FormatSize, Res.string.topic_menu_item_text_size),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicTopBar(
    isLoggedIn: Boolean,
    topicInfo: TopicInfoWrapper,
    showTopicTitle: Boolean,
    onBackClick: () -> Unit,
    onMenuClick: (TopicMenuItem) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = with(topicInfo.topic?.headerInfo?.title) {
                    if (showTopicTitle && this != null) this else stringResource(Res.string.topic)
                },
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = { BackIcon(onBackClick) },
        actions = {
            TopicTopBarActions(isLoggedIn, topicInfo, onMenuClick)
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun TopicTopBarActions(
    isLoggedIn: Boolean,
    topicInfo: TopicInfoWrapper,
    onMenuClick: (TopicMenuItem) -> Unit
) {
    val contentColor = LocalContentColor.current
    if (isLoggedIn) {
        val topicMenuItem = remember(topicInfo) {
            if (topicInfo.isFavorited) TopicMenuItem.Favorited else TopicMenuItem.Favorite
        }
        TextButton(
            onClick = { onMenuClick(topicMenuItem) },
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 48.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    topicMenuItem.icon,
                    contentDescription = topicMenuItem.name,
                    tint = contentColor
                )
                val favoriteCount = topicInfo.favoriteCount
                if (favoriteCount > 0) {
                    Text(
                        favoriteCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }

    }

    var moreExpanded by remember { mutableStateOf(false) }
    IconButton(onClick = { moreExpanded = true }) {
        Icon(TopicMenuItem.More.icon, contentDescription = "more")
    }

    val topicMenuItems: List<TopicMenuItem> = remember(isLoggedIn, topicInfo) {
        mutableListOf<TopicMenuItem>().apply {
            if (isLoggedIn) {
                if (topicInfo.topic != null) {
                    val headerInfo = topicInfo.topic.headerInfo
                    if (headerInfo != null && headerInfo.canAppend()) {
                        add(TopicMenuItem.Append)
                    }
                    add(if (topicInfo.isThanked) TopicMenuItem.Thanked else TopicMenuItem.Thanks)
                }
                add(if (topicInfo.isIgnored) TopicMenuItem.Ignored else TopicMenuItem.Ignore)
                // 网站已经取消了举报入口，当前客户端也屏蔽举报按钮
//                if (topicInfo.topic?.hasReportPermission() == true) {
//                    add(if (topicInfo.isReported) TopicMenuItem.Reported else TopicMenuItem.Report)
//                }
            }
            addAll(listOf(TopicMenuItem.Share, TopicMenuItem.OpenInBrowser, TopicMenuItem.TextSize))
        }
    }
    DropdownMenu(expanded = moreExpanded, onDismissRequest = { moreExpanded = false }) {
        topicMenuItems.forEach { menuItem ->
            DropdownMenuItem(
                text = { Text(stringResource(menuItem.label)) },
                leadingIcon = {
                    Icon(menuItem.icon, menuItem.name)
                },
                onClick = {
                    onMenuClick(menuItem)
                    moreExpanded = false
                })
        }
    }
}