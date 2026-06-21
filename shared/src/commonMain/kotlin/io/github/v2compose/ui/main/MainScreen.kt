package io.github.v2compose.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.network.bean.RecentTopics
import io.github.v2compose.ui.HandleSnackbarMessage
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.github.v2compose.ui.common.PlatformBackHandler
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
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.main_home
import v2compose.shared.generated.resources.main_mine
import v2compose.shared.generated.resources.main_nodes
import v2compose.shared.generated.resources.main_notifications

@Composable
fun MainScreenRoute(
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onTopicIdClick: (String) -> Unit,
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
    onAppearanceSettingsClick: () -> Unit,
    onHomeTabSettingsClick: () -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    viewModel: MainViewModel = koinViewModel(),
) {
    val unreadNotifications by viewModel.unreadNotifications.collectAsStateWithLifecycle()
    val loadNodesState by viewModel.loadNodes.state.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val hideLoginRelatedUi by viewModel.hideLoginRelatedUi.collectAsStateWithLifecycle()

    HandleSnackbarMessage(viewModel)

    val clickDispatcher = remember { ClickDispatcher() }

    CompositionLocalProvider(LocalClickDispatcher provides clickDispatcher) {
        MainScreen(
            selectedTabIndex = selectedTabIndex,
            unreadNotifications = unreadNotifications,
            loadNodesState = loadNodesState,
            hideLoginRelatedUi = hideLoginRelatedUi,
            onSearchClick = onSearchClick,
            onSettingsClick = onSettingsClick,
            onAppearanceSettingsClick = onAppearanceSettingsClick,
            onHomeTabSettingsClick = onHomeTabSettingsClick,
            onNewsItemClick = onNewsItemClick,
            onRecentItemClick = onRecentItemClick,
            onTopicIdClick = onTopicIdClick,
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
            onBottomTabClick = viewModel::updateSelectedTabIndex,
            onBottomTabClickAgain = clickDispatcher::dispatch
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    selectedTabIndex: Int,
    unreadNotifications: Int,
    loadNodesState: LoadNodesState,
    hideLoginRelatedUi: Boolean,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAppearanceSettingsClick: () -> Unit,
    onHomeTabSettingsClick: () -> Unit,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onTopicIdClick: (String) -> Unit,
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
    onBottomTabClick: (Int) -> Unit,
    onBottomTabClickAgain: () -> Unit,
) {
    var navBarSelectedIndex by rememberSaveable { mutableIntStateOf(selectedTabIndex) }

    // Hide notifications tab (index=2) when login-related UI is disabled.
    LaunchedEffect(hideLoginRelatedUi) {
        if (hideLoginRelatedUi && navBarSelectedIndex == MainBottomTab.Notifications.ordinal) {
            navBarSelectedIndex = MainBottomTab.Home.ordinal
        }
    }
    var showNodes by rememberSaveable { mutableStateOf(false) }
    val hasNodes = rememberSaveable(loadNodesState) { loadNodesState is LoadNodesState.Success }
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 切换 tab 时重置顶栏状态，确保顶栏展开显示
    LaunchedEffect(navBarSelectedIndex) {
        topAppBarScrollBehavior.state.heightOffset = 0f
    }

    PlatformBackHandler(enabled = showNodes) {
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
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
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
                val isHomeTab = navBarSelectedIndex == MainBottomTab.Home.ordinal
                MainContent(
                    navBarSelectedIndex = navBarSelectedIndex,
                    hideLoginRelatedUi = hideLoginRelatedUi,
                    nestedScrollConnection = if (isHomeTab) topAppBarScrollBehavior.nestedScrollConnection else null,
                    onNewsItemClick = onNewsItemClick,
                    onRecentItemClick = onRecentItemClick,
                    onTopicIdClick = onTopicIdClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    onLoginClick = onLoginClick,
                    onMyHomePageClick = onMyHomePageClick,
                    onCreateTopicClick = onCreateTopicClick,
                    onMyNodesClick = onMyNodesClick,
                    onMyTopicsClick = onMyTopicsClick,
                    onMyFollowingClick = onMyFollowingClick,
                    onSettingsClick = onSettingsClick,
                    onAppearanceSettingsClick = onAppearanceSettingsClick,
                    onHomeTabSettingsClick = onHomeTabSettingsClick,
                    onUriClick = onUriClick,
                    onHtmlImageClick = onHtmlImageClick,
                )
            }
            MainBottomNavigation(
                selectedIndex = navBarSelectedIndex,
                unreadNotifications = unreadNotifications,
                hideLoginRelatedUi = hideLoginRelatedUi,
                onItemSelected = { index ->
                    onBottomTabClick(index)
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
    Search(Icons.Rounded.Search),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentNavBarIndex: Int,
    onMenuItemClick: (MenuItem) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val navBarItemNames = listOf(
        stringResource(Res.string.main_home),
        stringResource(Res.string.main_nodes),
        stringResource(Res.string.main_notifications),
        stringResource(Res.string.main_mine)
    )
    val menuItem = remember(currentNavBarIndex) {
        when (currentNavBarIndex) {
            // Mine tab has its own entry to Settings; hide top-right Settings icon there.
            3 -> null
            else -> MenuItem.Search
        }
    }
    CenterAlignedTopAppBar(
        modifier = Modifier.heightIn(min = 44.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        title = {
            Text(
                navBarItemNames[currentNavBarIndex],
                style = MaterialTheme.typography.titleMedium,
            )
        },
        actions = {
            menuItem?.let {
                IconButton(onClick = { onMenuItemClick(it) }) {
                    Icon(
                        it.imageVector,
                        contentDescription = it.name,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun MainContent(
    navBarSelectedIndex: Int,
    hideLoginRelatedUi: Boolean,
    nestedScrollConnection: NestedScrollConnection? = null,
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onTopicIdClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    onLoginClick: () -> Unit,
    onMyHomePageClick: () -> Unit,
    onCreateTopicClick: () -> Unit,
    onMyNodesClick: () -> Unit,
    onMyTopicsClick: () -> Unit,
    onMyFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAppearanceSettingsClick: () -> Unit,
    onHomeTabSettingsClick: () -> Unit,
    onUriClick: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    modifier: Modifier = Modifier,
) {
    rememberSaveableStateHolder().SaveableStateProvider(key = navBarSelectedIndex) {
        when (navBarSelectedIndex) {
            0 -> HomeContent(
                onNewsItemClick = onNewsItemClick,
                onRecentItemClick = onRecentItemClick,
                onTopicIdClick = onTopicIdClick,
                onNodeClick = onNodeClick,
                onUserAvatarClick = onUserAvatarClick,
                nestedScrollConnection = nestedScrollConnection,
            )

            1 -> NodesContent(onNodeClick = onNodeClick, modifier = modifier)
            2 -> {
                if (!hideLoginRelatedUi) {
                    NotificationsContent(
                        onLoginClick = onLoginClick,
                        onUriClick = onUriClick,
                        onUserAvatarClick = onUserAvatarClick,
                        onHtmlImageClick = onHtmlImageClick,
                        modifier = modifier,
                    )
                }
            }

            3 -> MineContent(
                onLoginClick = onLoginClick,
                onMyHomePageClick = onMyHomePageClick,
                onCreateTopicClick = onCreateTopicClick,
                onMyNodesClick = onMyNodesClick,
                onMyTopicsClick = onMyTopicsClick,
                onMyFollowingClick = onMyFollowingClick,
                onSettingsClick = onSettingsClick,
                onAppearanceSettingsClick = onAppearanceSettingsClick,
                onHomeTabSettingsClick = onHomeTabSettingsClick,
                hideLoginRelatedUi = hideLoginRelatedUi,
                modifier = modifier,
            )

            else -> Unit
        }
    }
}

@Composable
fun MainBottomNavigation(
    selectedIndex: Int,
    unreadNotifications: Int,
    hideLoginRelatedUi: Boolean,
    onItemSelected: (Int) -> Unit,
) {
    NavigationBar(modifier = Modifier.height(56.dp)) {
        val tabs = remember(hideLoginRelatedUi) {
            MainBottomTab.values().filterNot { hideLoginRelatedUi && it == MainBottomTab.Notifications }
        }
        tabs.forEach { item ->
            val tabIndex = item.ordinal
            NavigationBarItem(
                icon = {
                    if (tabIndex == MainBottomTab.Notifications.ordinal && unreadNotifications > 0) {
                        BadgedBox(badge = { Badge { Text(unreadNotifications.toString()) } }) {
                            Icon(item.icon, contentDescription = item.name)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.name)
                    }
                },
                alwaysShowLabel = false,
                selected = tabIndex == selectedIndex,
                onClick = { onItemSelected(tabIndex) },
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
