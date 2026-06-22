package io.github.v2compose.ui.main.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.network.bean.RecentTopics
import io.github.v2compose.ui.common.MyScrollableTabRow
import io.github.v2compose.ui.main.home.HomeTabConfig.Companion.decodeList
import io.github.v2compose.ui.main.home.HomeTabConfig.Companion.defaultTabs
import io.github.v2compose.ui.main.home.effectiveHomeTabs
import io.github.v2compose.ui.main.home.recent.RecentTab
import io.github.v2compose.ui.main.home.tab.NewsTab
import io.github.v2compose.ui.main.home.node.HomeNodeTab
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import io.github.v2compose.datasource.AppPreferences

private val TabRowBaseHeight = 32.dp

private const val RecentTabValue = "recent"

@Composable
fun HomeContent(
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onTopicIdClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val appPreferences: AppPreferences = koinInject()
    val appSettings by appPreferences.appSettings.collectAsState(initial = io.github.v2compose.shared.bean.AppSettings.Default)
    val tabRowHeight = (TabRowBaseHeight.value + appSettings.homeTabRowTextVerticalPadding * 2).dp

    val configuredTabs = remember(appSettings.homeTabConfigsJson) {
        decodeList(appSettings.homeTabConfigsJson)
    }
    val tabInfos = configuredTabs.effectiveHomeTabs()
    val pagerState = rememberPagerState { tabInfos.size }

    // When tabs are reordered/changed from settings, always default to the first tab.
    // This matches user expectation: open Home shows the first category.
    val tabKey = remember(tabInfos) { tabInfos.joinToString(separator = "|") { it.id } }
    LaunchedEffect(tabKey) {
        if (pagerState.currentPage != 0) {
            pagerState.scrollToPage(0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = tabRowHeight),
            state = pagerState,
            key = { tabInfos[it].id },
        ) { pageIndex ->
            val tabInfo = tabInfos[pageIndex]
            if (tabInfo.isNodeTab()) {
                HomeNodeTab(
                    nodeName = tabInfo.nodeName!!,
                    nodeTitle = tabInfo.name,
                    onTopicIdClick = onTopicIdClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    nestedScrollConnection = nestedScrollConnection,
                )
            } else if (tabInfo.newsTabValue == RecentTabValue) {
                RecentTab(
                    onRecentItemClick = onRecentItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    nestedScrollConnection = nestedScrollConnection,
                )
            } else {
                NewsTab(
                    newsTabInfo = NewsTabInfo(tabInfo.name, tabInfo.newsTabValue ?: "all"),
                    onNewsItemClick = onNewsItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
                    nestedScrollConnection = nestedScrollConnection,
                )
            }
        }

        MyScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 12.dp,
            minItemWidth = 0.dp,
        ) {
            tabInfos.forEachIndexed { index, tabInfo ->
                val selected = index == pagerState.currentPage
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    },
                    modifier = Modifier.height(tabRowHeight)
                ) {
                    Text(
                        tabInfo.name,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = appSettings.homeTabRowTextVerticalPadding.dp)
                    )
                }
            }
        }
    }
}

data class NewsTabInfo(val name: String, val value: String)
