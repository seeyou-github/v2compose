package io.github.v2compose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.v2compose.Constants
import io.github.v2compose.LocalAppSettings
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.n_comment
import v2compose.shared.generated.resources.n_views

@Composable
fun SimpleTopic(
    userName: String,
    userAvatar: String,
    time: String,
    replyCount: String,
    viewCount: Int? = null,
    nodeName: String,
    nodeTitle: String,
    title: String,
    titleOverview: Boolean = false,
    hideUserInfo: Boolean = false,
    onItemClick: (() -> Unit)? = null,
    onUserAvatarClick: (() -> Unit)? = null,
    onNodeClick: (() -> Unit)? = null,
) {
    val appSettings = LocalAppSettings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onItemClick != null) { onItemClick?.invoke() },
    ) {
        Column(
            Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = appSettings.homeListItemVerticalPadding.dp,
                bottom = appSettings.homeListItemVerticalPadding.dp,
            )
        ) {
            if (!hideUserInfo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TopicUserAvatar(
                        userName = userName,
                        userAvatar = userAvatar,
                        onUserAvatarClick = onUserAvatarClick,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Center,
                                    trim = LineHeightStyle.Trim.FirstLineTop,
                                ),
                            ),
                        )

                        Row {
                            Text(
                                time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                stringResource(Res.string.n_comment, replyCount.ifBlank { "0" }),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            viewCount?.let {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    stringResource(Res.string.n_views, viewCount.toString()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    NodeTag(
                        nodeTitle = nodeTitle,
                        nodeName = nodeName,
                        onItemClick = { _, _ -> onNodeClick?.invoke() },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = LocalAppSettings.current.topicListTitleTextSize.sp,
                    lineHeight = LocalAppSettings.current.homeListTitleLineHeight.sp,
                ),
                maxLines = if (titleOverview) Constants.topicTitleOverviewMaxLines else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )

            if (hideUserInfo) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.n_comment, replyCount.ifBlank { "0" }),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        ListDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun TopicUserAvatar(
    userName: String,
    userAvatar: String,
    modifier: Modifier = Modifier,
    onUserAvatarClick: (() -> Unit)? = null,
) {
    val disableAvatarImages = LocalAppSettings.current.disableAvatarImages
    if (!disableAvatarImages) {
        AsyncImage(
            model = userAvatar,
            contentDescription = "$userName's avatar",
            modifier = modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                .clickable(enabled = onUserAvatarClick != null) { onUserAvatarClick?.invoke() },
            contentScale = ContentScale.Crop,
        )
        return
    }

    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.surface.luminance() < 0.5f
    val placeholderBg = remember(userName, isDark) { avatarPlaceholderColor(userName, isDark) }
    val letter = remember(userName) { userName.trim().firstOrNull()?.toString().orEmpty() }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(placeholderBg)
            .clickable(enabled = onUserAvatarClick != null) { onUserAvatarClick?.invoke() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelLarge,
            color = if (isDark) Color.White else Color.Black,
            maxLines = 1,
        )
    }
}

private fun avatarPlaceholderColor(userName: String, dark: Boolean): Color {
    // Stable per username.
    val seed = userName.trim().lowercase().hashCode()
    val h = ((seed % 360) + 360) % 360
    val s = if (dark) 0.55f else 0.45f
    val l = if (dark) 0.40f else 0.75f
    return hslToColor(h.toFloat(), s, l)
}

// Minimal HSL -> RGB conversion; avoids extra deps.
private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val hh = (h / 60f) % 6f
    val x = c * (1f - kotlin.math.abs(hh % 2f - 1f))
    val (r1, g1, b1) = when {
        hh < 1f -> Triple(c, x, 0f)
        hh < 2f -> Triple(x, c, 0f)
        hh < 3f -> Triple(0f, c, x)
        hh < 4f -> Triple(0f, x, c)
        hh < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    return Color(r1 + m, g1 + m, b1 + m, 1f)
}
