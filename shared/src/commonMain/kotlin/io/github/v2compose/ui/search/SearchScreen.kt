package io.github.v2compose.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.v2compose.core.extension.toTimeText
import io.github.v2compose.network.bean.SoV2EXSearchResultInfo
import io.github.v2compose.ui.common.pagingAppendMoreItem
import io.github.v2compose.ui.common.pagingRefreshItem
import io.github.v2compose.ui.common.rememberLazyListState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.history_keywords
import v2compose.shared.generated.resources.logo_sov2ex
import v2compose.shared.generated.resources.search_user_time_replies

@Composable
fun SearchScreenRoute(
    goBack: () -> Unit,
    onTopicClick: (SoV2EXSearchResultInfo.Hit) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val keyword by viewModel.keyword.collectAsStateWithLifecycle()
    val topics = viewModel.topics.collectAsLazyPagingItems()
    val historyKeywords by viewModel.historyKeywords.collectAsStateWithLifecycle()

    SearchScreen(
        keyword = keyword,
        historyKeywords = historyKeywords,
        topics = topics,
        onCloseClick = goBack,
        onTopicClick = onTopicClick,
        onSearchClick = viewModel::search,
        onDeleteKeywordsClick = viewModel::clearHistoryKeywords,
    )
}

@Composable
private fun SearchScreen(
    keyword: String?,
    historyKeywords: List<String>,
    topics: LazyPagingItems<SoV2EXSearchResultInfo.Hit>,
    onCloseClick: () -> Unit,
    onTopicClick: (SoV2EXSearchResultInfo.Hit) -> Unit,
    onSearchClick: (String) -> Unit,
    onDeleteKeywordsClick: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)

    Scaffold(modifier = Modifier.background(color = backgroundColor)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = backgroundColor)
                .padding(it),
        ) {
            if (topics.itemSnapshotList.isEmpty()) {
                SearchHistoryKeywords(
                    searchKeywords = historyKeywords,
                    onKeywordClick = onSearchClick,
                    onDeleteKeywordsClick = onDeleteKeywordsClick,
                    modifier = Modifier.padding(top = 72.dp),
                )
            } else {
                SearchResult(
                    topics = topics,
                    onTopicClick = onTopicClick,
                )
            }
            SearchBar(
                keyword = keyword,
                onCloseClick = onCloseClick,
                onSearchClick = onSearchClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    keyword: String?,
    onCloseClick: () -> Unit,
    onSearchClick: (String) -> Unit,
) {
    var currentKeyword by remember(keyword) {
        mutableStateOf(TextFieldValue(keyword ?: "", selection = TextRange(keyword?.length ?: 0)))
    }
    var autoShowKeyboard by rememberSaveable { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val onSearchAction = remember(onSearchClick, currentKeyword) {
        {
            if (currentKeyword.text.isNotEmpty()) {
                onSearchClick(currentKeyword.text)
                keyboard?.hide()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        OutlinedTextField(
            value = currentKeyword,
            onValueChange = { currentKeyword = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp),
                )
                .focusRequester(focusRequester)
                .onFocusChanged { },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
            singleLine = true,
            placeholder = {
                Icon(
                    painter = painterResource(Res.drawable.logo_sov2ex),
                    contentDescription = "sov2ex logo",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            },
            shape = RoundedCornerShape(12.dp),
        )
        IconButton(
            onClick = {
                if (currentKeyword.text.isNotEmpty()) {
                    currentKeyword = TextFieldValue("")
                } else {
                    onCloseClick()
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "close",
            )
        }
    }

    LaunchedEffect(focusRequester) {
        if (autoShowKeyboard) {
            focusRequester.requestFocus()
            keyboard?.show()
            autoShowKeyboard = false
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistoryKeywords(
    searchKeywords: List<String>,
    onKeywordClick: (String) -> Unit,
    onDeleteKeywordsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(Res.string.history_keywords),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
            )
            Icon(
                Icons.Rounded.Delete,
                contentDescription = "delete",
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .clickable { onDeleteKeywordsClick() }
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
        ) {
            searchKeywords.forEach {
                SearchKeyword(keyword = it, onKeywordClick = onKeywordClick)
            }
        }
    }
}

@Composable
private fun SearchKeyword(keyword: String, onKeywordClick: (String) -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    Box(modifier = Modifier.padding(6.dp)) {
        Box(
            modifier = Modifier
                .height(24.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(24.dp),
                )
                .clip(RoundedCornerShape(24.dp))
                .clickable { onKeywordClick(keyword) }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = keyword,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SearchResult(
    topics: LazyPagingItems<SoV2EXSearchResultInfo.Hit>,
    onTopicClick: (SoV2EXSearchResultInfo.Hit) -> Unit,
) {
    val lazyListState = topics.rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 72.dp),
        state = lazyListState,
    ) {
        pagingRefreshItem(topics)
        items(topics.itemCount, topics.itemKey { it.source.id }) { index ->
            val item = topics[index]
            item?.let {
                SearchTopic(topic = item, onTopicClick = onTopicClick)
            }
        }
        pagingAppendMoreItem(topics)
    }

    val isRefreshing = topics.loadState.refresh is LoadState.Loading
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            lazyListState.scrollToItem(0)
        }
    }
}

@Composable
private fun SearchTopic(
    topic: SoV2EXSearchResultInfo.Hit,
    onTopicClick: (SoV2EXSearchResultInfo.Hit) -> Unit,
) {
    val highlightContent = remember(topic) {
        val highlight = topic.highlight
        listOfNotNull(
            highlight?.content?.firstOrNull(),
            highlight?.postscriptListContent?.firstOrNull(),
            highlight?.replyListContent?.firstOrNull(),
        )
            .filter { it.isNotBlank() }
            .joinToString("...")
            .ifBlank { topic.source.content }
    }

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTopicClick(topic) }
                .background(color = MaterialTheme.colorScheme.background)
                .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            SearchTopicText(
                text = topic.highlight?.title?.firstOrNull() ?: topic.source.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            SearchTopicText(
                text = highlightContent,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.3.sp,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(
                    Res.string.search_user_time_replies,
                    topic.source.creator,
                    topic.source.time.toTimeText(),
                    topic.source.replies,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SearchTopicText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    val annotatedString = buildAnnotatedString {
        var start = 0
        while (true) {
            val index = text.indexOf("<em>", startIndex = start)
            if (index < 0 || start > index) {
                break
            }
            append(text.substring(start, index))
            val endIndex = text.indexOf("</em>", startIndex = start)
            if (endIndex < 0) {
                break
            }
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                append(text.substring(index + 4, endIndex))
            }
            start = endIndex + 5
        }
        if (start < text.length) {
            append(text.substring(start))
        }
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
    )
}
