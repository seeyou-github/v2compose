package io.github.v2compose.ui.main.home

import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.v2compose.network.bean.NewsInfo
import io.github.v2compose.network.bean.RecentTopics
import io.github.v2compose.ui.common.MyScrollableTabRow
import io.github.v2compose.ui.main.home.recent.RecentTab
import io.github.v2compose.ui.main.home.tab.NewsTab
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val TabRowHeight = 32.dp

private const val TAG = "HomeContent"

@Composable
fun HomeContent(
    onNewsItemClick: (NewsInfo.Item) -> Unit,
    onRecentItemClick: (RecentTopics.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {

    val coroutineScope = rememberCoroutineScope()
    val tabInfos = viewModel.newsTabInfos
    val pagerState = rememberPagerState { tabInfos.size }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = TabRowHeight),
            state = pagerState,
            key = { tabInfos[it].value },
        ) { pageIndex ->
            val tabInfo = tabInfos[pageIndex]
            if (tabInfo.value == NewsTabInfo.recent) {
                RecentTab(
                    onRecentItemClick = onRecentItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick
                )
            } else {
                NewsTab(
                    newsTabInfo = tabInfo,
                    onNewsItemClick = onNewsItemClick,
                    onNodeClick = onNodeClick,
                    onUserAvatarClick = onUserAvatarClick,
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
                            Log.d(TAG, "animateScrollToPage, index = $index")
                        }
                    },
                    modifier = Modifier.height(TabRowHeight)
                ) {
                    Text(
                        tabInfo.name,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
