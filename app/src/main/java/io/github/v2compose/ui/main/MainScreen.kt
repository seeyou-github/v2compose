package io.github.v2compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.network.bean.RecentTopics
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.NewReleaseDialog
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.github.v2compose.ui.common.SelectNode
import io.github.v2compose.ui.main.composables.ClickDispatcher
import io.github.v2compose.ui.main.composables.LocalClickDispatcher
import io.github.v2compose.ui.main.home.HomeContent
import io.github.v2compose.ui.main.mine.MineContent
import io.github.v2compose.ui.main.nodes.NodesContent
import io.github.v2compose.ui.main.notifications.NotificationsContent
import io.github.v2compose.usecase.LoadNodesState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.main_home
import v2compose.shared.generated.resources.main_mine
import v2compose.shared.generated.resources.main_nodes
import v2compose.shared.generated.resources.main_notifications

@Composable
fun MainScreenRoute(
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onSearchClick: () -> Unit,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    viewModel: MainViewModel = koinViewModel(),
    screenState: MainScreenState = rememberMainScreenState()
) {
    val unreadNotifications by viewModel.unreadNotifications.collectAsStateWithLifecycle()
    val loadNodesState by viewModel.loadNodes.state.collectAsStateWithLifecycle()

    HandleSnackbarMessage(viewModel, screenState)

    val newRelease by viewModel.newRelease.collectAsStateWithLifecycle()
    if (newRelease.isValid()) {
        NewReleaseDialog(
            release = newRelease,
            onIgnoreClick = {
                viewModel.ignoreRelease(newRelease)
                viewModel.resetNewRelease()
            },
            onCancelClick = viewModel::resetNewRelease,
            onOkClick = {
                openUri(newRelease.htmlUrl)
                viewModel.resetNewRelease()
            },
        )
    }

    val clickDispatcher = remember { ClickDispatcher() }

    CompositionLocalProvider(LocalClickDispatcher provides clickDispatcher) {
        MainScreen(
            unreadNotifications = unreadNotifications,
            loadNodesState = loadNodesState,
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onNewsItemClick = onNewsItemClick,
            onRecentItemClick = onRecentItemClick,
            onNodeClick = onNodeClick,
            onUserAvatarClick = onUserAvatarClick,
            onLoginClick = onLoginClick,
            onMyHomePageClick = onMyHomePageClick,
            onCreateTopicClick = onCreateTopicClick,
            onMyNodesClick = onMyNodesClick,
            onMyTopicsClick = onMyTopicsClick,
            onMyFollowingClick = onMyFollowingClick,
            onUriClick = openUri,
            onHtmlImageClick = onHtmlImageClick,
            loadNodes = viewModel::loadNodes,
            onBottomTabClickAgain = clickDispatcher::dispatch
        )
    }

}

@Composable
private fun MainScreen(
    unreadNotifications: Int,
    loadNodesState: LoadNodesState,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onUriClick: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    loadNodes: () -> Unit,
    onBottomTabClickAgain: () -> Unit,
) {
    var navBarSelectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var showNodes by rememberSaveable { mutableStateOf(false) }
    val hasNodes = rememberSaveable(loadNodesState) { loadNodesState is LoadNodesState.Success }

    BackHandler(enabled = showNodes) {
        showNodes = false
    }

    Scaffold(
        topBar = {
            MainTopBar(
                currentNavBarIndex = navBarSelectedIndex,
                onMenuItemClick = {
                    when (it) {
                        MenuItem.Search -> {
                            if (navBarSelectedIndex == MainBottomTab.Nodes.ordinal) {
                                showNodes = true
                                if (!hasNodes) loadNodes()
                            } else {
                                onSearchClick()
                            }
                        }

                        MenuItem.Settings -> onSettingsClick()
                    }
                },
            )
        }, contentWindowInsets = WindowInsets(bottom = 0)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                MainContent(
                    navBarSelectedIndex = navBarSelectedIndex,
                    onNewsItemClick = onNewsItemClick,
                    onRecentItemClick = onRecentItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    onLoginClick = onLoginClick,
                    onMyHomePageClick = onMyHomePageClick,
                    onCreateTopicClick = onCreateTopicClick,
                    onMyNodesClick = onMyNodesClick,
                    onMyTopicsClick = onMyTopicsClick,
                    onMyFollowingClick = onMyFollowingClick,
                    onSettingsClick = onSettingsClick,
                    onUriClick = onUriClick,
                    onHtmlImageClick = onHtmlImageClick,
                )
            }
            MainBottomNavigation(
                selectedIndex = navBarSelectedIndex,
                unreadNotifications = unreadNotifications,
                onItemSelected = { index ->
                    if (index == navBarSelectedIndex) onBottomTabClickAgain()
                    navBarSelectedIndex = index
                })
        }
    }

    if (showNodes && hasNodes) {
        val nodes = (loadNodesState as LoadNodesState.Success).data
        SelectNode(
            nodes = nodes,
            onNodeClick = {
                showNodes = false
                onNodeClick(it.name, it.title)
            },
            onDismiss = { showNodes = false },
        )
    }

}

private enum class MenuItem(val imageVector: ImageVector) {
    Search(Icons.Rounded.Search), Settings(Icons.Rounded.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentNavBarIndex: Int,
    onMenuItemClick: (MenuItem) -> Unit,
) {
    val navBarItemNames = listOf(
        stringResource(Res.string.main_home),
        stringResource(Res.string.main_nodes),
        stringResource(Res.string.main_notifications),
        stringResource(Res.string.main_mine)
    )
    val menuItem = remember(currentNavBarIndex) {
        when (currentNavBarIndex) {
            3 -> MenuItem.Settings
            else -> MenuItem.Search
        }
    }
    CenterAlignedTopAppBar(
        title = { Text(navBarItemNames[currentNavBarIndex]) },
        actions = {
            IconButton(onClick = { onMenuItemClick(menuItem) }) {
                Icon(
                    menuItem.imageVector,
                    contentDescription = menuItem.name,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
    )
}

@Composable
fun MainContent(
    navBarSelectedIndex: Int,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUriClick: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    modifier: Modifier = Modifier,
) {
    rememberSaveableStateHolder().SaveableStateProvider(key = navBarSelectedIndex) {
        when (navBarSelectedIndex) {
            0 -> HomeContent(
                onNewsItemClick = onNewsItemClick,
                onRecentItemClick = onRecentItemClick,
                onNodeClick = onNodeClick,
                onUserAvatarClick = onUserAvatarClick,
            )

            1 -> NodesContent(onNodeClick = onNodeClick, modifier = modifier)
            2 -> NotificationsContent(
                onLoginClick = onLoginClick,
                onUriClick = onUriClick,
                onUserAvatarClick = onUserAvatarClick,
                onHtmlImageClick = onHtmlImageClick,
                modifier = modifier,
            )

            3 -> MineContent(
                onLoginClick = onLoginClick,
                onMyHomePageClick = onMyHomePageClick,
                onCreateTopicClick = onCreateTopicClick,
                onMyNodesClick = onMyNodesClick,
                onMyTopicsClick = onMyTopicsClick,
                onMyFollowingClick = onMyFollowingClick,
                onSettingsClick = onSettingsClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun MainBottomNavigation(
    selectedIndex: Int, unreadNotifications: Int, onItemSelected: (Int) -> Unit
) {
    NavigationBar {
        MainBottomTab.values().forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (index == 2 && unreadNotifications > 0) {
                        BadgedBox(badge = { Badge { Text(unreadNotifications.toString()) } }) {
                            Icon(item.icon, contentDescription = item.name)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.name)
                    }
                },
                label = { Text(stringResource(item.title)) },
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
            )
        }
    }
}

enum class MainBottomTab(val title: StringResource, val icon: ImageVector) {
    Home(Res.string.main_home, Icons.Outlined.Home), Nodes(
        Res.string.main_nodes,
        Icons.AutoMirrored.Outlined.List
    ),
    Notifications(
        Res.string.main_notifications,
        Icons.Outlined.Notifications
    ),
    Mine(Res.string.main_mine, Icons.Outlined.Person)
}