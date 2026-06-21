package io.github.v2compose.ui.main.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Topic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.v2compose.LocalSnackbarHostState
import io.github.v2compose.core.extension.isBeforeTodayByUTC
import io.github.v2compose.shared.bean.Account
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.ListDivider
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.bronze
import v2compose.shared.generated.resources.checking_in
import v2compose.shared.generated.resources.create_topic
import v2compose.shared.generated.resources.daily_mission
import v2compose.shared.generated.resources.daily_mission_ok
import v2compose.shared.generated.resources.gold
import v2compose.shared.generated.resources.login
import v2compose.shared.generated.resources.login_first
import v2compose.shared.generated.resources.my_following
import v2compose.shared.generated.resources.my_nodes
import v2compose.shared.generated.resources.my_topics
import v2compose.shared.generated.resources.settings
import v2compose.shared.generated.resources.silver


@Composable
fun MineContent(
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hideLoginRelatedUi: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MineViewModel = koinViewModel(),
) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val lastCheckInTime by viewModel.lastCheckInTime.collectAsStateWithLifecycle()
    val hasCheckingInTips by viewModel.hasCheckingInTips.collectAsStateWithLifecycle()
    val checkingIn by viewModel.checkingIn.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    HandleSnackbarMessage(viewModel)

    LaunchedEffect(true) {
        viewModel.refreshAccount()
    }

    fun doActionIfLoggedIn(action: () -> Unit) {
        if (account.isValid()) {
            action()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(getString(Res.string.login_first))
            }
        }
    }

    MineContainer(
        account = account,
        lastCheckInTime = lastCheckInTime,
        hasCheckingInTips = hasCheckingInTips,
        checkingIn = checkingIn,
        onLoginClick = onLoginClick,
        onMyHomePageClick = onMyHomePageClick,
        onCheckInClick = viewModel::doCheckIn,
        onCreateTopicClick = { doActionIfLoggedIn(onCreateTopicClick) },
        onMyNodesClick = { doActionIfLoggedIn(onMyNodesClick) },
        onMyTopicsClick = { doActionIfLoggedIn(onMyTopicsClick) },
        onMyFollowingClick = { doActionIfLoggedIn(onMyFollowingClick) },
        onSettingsClick = onSettingsClick,
        hideLoginRelatedUi = hideLoginRelatedUi,
        modifier = Modifier
    )
}

@Composable
private fun MineContainer(
    account: Account,
    lastCheckInTime: Long,
    hasCheckingInTips: Boolean,
    checkingIn: Boolean,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hideLoginRelatedUi: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))
    ) {
        Column {
            // When hiding login-related UI, also hide the login entry in Mine header.
            if (!hideLoginRelatedUi || account.isValid()) {
                MineHeader(
                    account = account,
                    lastCheckInTime = lastCheckInTime,
                    hasCheckingInTips = hasCheckingInTips,
                    checkingIn = checkingIn,
                    hideLoginRelatedUi = hideLoginRelatedUi,
                    onLoginClick = onLoginClick,
                    onMyHomePageClick = onMyHomePageClick,
                    onCheckInClick = onCheckInClick,
                )
            }
            Spacer(Modifier.height(8.dp))
            if (!hideLoginRelatedUi) {
                MineEntry(
                    leadingIcon = Icons.Rounded.Edit,
                    title = stringResource(Res.string.create_topic),
                    onEntryClick = onCreateTopicClick,
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                )
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                    MineEntry(
                        leadingIcon = Icons.Rounded.Category,
                        title = stringResource(Res.string.my_nodes),
                        subtitle = account.nodes.toString(),
                        onEntryClick = onMyNodesClick,
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                    )
                    ListDivider()
                    MineEntry(
                        leadingIcon = Icons.Rounded.Topic,
                        title = stringResource(Res.string.my_topics),
                        subtitle = account.topics.toString(),
                        onEntryClick = onMyTopicsClick,
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                    )
                    ListDivider()
                    MineEntry(
                        leadingIcon = Icons.Rounded.People,
                        title = stringResource(Res.string.my_following),
                        subtitle = account.following.toString(),
                        onEntryClick = onMyFollowingClick,
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            MineEntry(
                leadingIcon = Icons.Rounded.Settings,
                title = stringResource(Res.string.settings),
                onEntryClick = onSettingsClick,
                modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
            )
        }
    }
}

@Composable
private fun MineHeader(
    account: Account,
    lastCheckInTime: Long,
    hasCheckingInTips: Boolean,
    checkingIn: Boolean,
    hideLoginRelatedUi: Boolean,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCheckInClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(enabled = account.isValid() || !hideLoginRelatedUi) {
                if (account.isValid()) {
                    onMyHomePageClick()
                } else if (!hideLoginRelatedUi) {
                    onLoginClick()
                }
            }
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = account.userAvatar,
            contentDescription = "user avatar",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (account.isValid()) account.userName else stringResource(Res.string.login),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 0.dp)
                        .wrapContentHeight()
                )
                if (account.isValid()) {
                    Spacer(Modifier.width(16.dp))
                    AccountBalanceItem(num = account.balance.gold, icon = Res.drawable.gold)
                    AccountBalanceItem(num = account.balance.silver, icon = Res.drawable.silver)
                    AccountBalanceItem(num = account.balance.bronze, icon = Res.drawable.bronze)
                }
            }
            if (account.isValid()) {
                CheckInButton(
                    hasCheckingInTips,
                    lastCheckInTime,
                    checkingIn,
                    onCheckInClick,
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Rounded.NavigateNext,
            "goto user center",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun AccountBalanceItem(num: Int, icon: DrawableResource) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp)
    ) {
        Text(
            num.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(2.dp))
        Image(painter = painterResource(icon), contentDescription = "")
    }
}

@Composable
private fun CheckInButton(
    hasCheckingInTips: Boolean,
    lastCheckInTime: Long,
    checkingIn: Boolean,
    onCheckInClick: () -> Unit,
) {
    val canCheckIn = hasCheckingInTips || lastCheckInTime.isBeforeTodayByUTC()
    val checkInText = if (checkingIn) {
        Res.string.checking_in
    } else if (canCheckIn) {
        Res.string.daily_mission
    } else {
        Res.string.daily_mission_ok
    }
    AssistChip(
        onClick = onCheckInClick,
        enabled = canCheckIn,
        label = {
            Text(
                stringResource(checkInText),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingIcon = {
            val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
            if (checkingIn) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = iconColor,
                    strokeWidth = 2.dp
                )
            } else if (!canCheckIn) {
                Icon(Icons.Rounded.Check, "daily mission", tint = iconColor)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MineEntry(
    leadingIcon: ImageVector,
    title: String,
    onEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clickable { onEntryClick() }
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            leadingIcon,
            "icon",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (!subtitle.isNullOrEmpty()) {
            Spacer(Modifier.width(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Rounded.NavigateNext,
            "enter",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
