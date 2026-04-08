package io.github.v2compose.ui.common


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.imageLoader
import io.github.v2compose.R
import io.github.v2compose.shared.bean.ContentFormat
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlinx.coroutines.launch

private val ContentBarHeight = 40.dp

@Composable
fun TextEditor(
    content: String,
    placeholder: String,
    contentFormat: ContentFormat,
    onContentChanged: (content: String) -> Unit,
    onContentFormatChanged: (format: ContentFormat) -> Unit,
    modifier: Modifier = Modifier,
    contentFocusRequester: FocusRequester = remember { FocusRequester() },
) {
    val tabTitles = remember(contentFormat) {
        if (contentFormat == ContentFormat.Markdown) {
            listOf(R.string.content_body, R.string.content_preview)
        } else {
            listOf(R.string.content_body)
        }
    }
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            key = { tabTitles[it] },
            modifier = Modifier.padding(top = ContentBarHeight)
        ) { index ->
            when (index) {
                0 -> ContentEditor(
                    content = content,
                    placeholder = placeholder,
                    onContentChanged = onContentChanged,
                    modifier = Modifier.focusRequester(contentFocusRequester),
                )

                1 -> MarkdownPreview(content)
            }
        }

        ContentBar(tabTitles, contentFormat, pagerState, onContentFormatChanged)

        ListDivider(modifier = Modifier.padding(top = ContentBarHeight))
    }
}

@Composable
private fun ContentBar(
    tabTitles: List<Int>,
    contentFormat: ContentFormat,
    pagerState: PagerState,
    onContentFormatChanged: (ContentFormat) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(ContentBarHeight)
            .background(color = MaterialTheme.colorScheme.background),
    ) {
        val currentPage =
            if (pagerState.currentPage >= tabTitles.size) 0 else pagerState.currentPage
        TabRow(
            selectedTabIndex = currentPage,
            modifier = Modifier.width(64.dp * tabTitles.size),
            divider = {},
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = index == currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier.height(ContentBarHeight),
                ) {
                    Text(stringResource(id = title))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val segments = listOf(ContentFormat.Original, ContentFormat.Markdown)
        var selectedSegment by remember(contentFormat) { mutableStateOf(contentFormat) }
        SegmentedControl(
            segments = segments,
            selectedSegment = selectedSegment,
            onSegmentSelected = {
                selectedSegment = it
                onContentFormatChanged(it)
            },
            modifier = Modifier.sizeIn(maxWidth = 192.dp),
        ) {
            val segmentResId = when (it) {
                ContentFormat.Original -> R.string.content_format_original
                ContentFormat.Markdown -> R.string.content_format_markdown
            }
            Text(stringResource(segmentResId), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ContentEditor(
    content: String,
    placeholder: String,
    onContentChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onContentChanged(it.text)
        },
        modifier = modifier.fillMaxSize(),
        colors = TextFieldDefaults.colors(
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            errorPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = placeholderColor,
        ),
        placeholder = { Text(placeholder) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.3.sp,
        ),
    )
}

@Composable
private fun MarkdownPreview(content: String) {
    val html = remember(content) {
        val flavour = CommonMarkFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(content)
        HtmlGenerator(content, parsedTree, flavour).generateHtml()
    }
    HtmlContent(
        content = html,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    )
}

@Preview(widthDp = 440, heightDp = 960, device = "id:Nexus 5")
@Composable
private fun TextEditorPreview() {
    TextEditor(
        content = "",
        placeholder = "",
        contentFormat = ContentFormat.Markdown,
        onContentChanged = {},
        onContentFormatChanged = {},
        modifier = Modifier.fillMaxSize(),
    )
}