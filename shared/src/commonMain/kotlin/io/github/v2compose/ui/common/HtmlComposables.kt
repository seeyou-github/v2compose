package io.github.v2compose.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import io.github.cooaer.htmltext.HtmlText
import io.github.cooaer.htmltext.Img
import io.github.v2compose.Constants

typealias OnHtmlImageClick = (String, List<String>) -> Unit

@Composable
fun HtmlContent(
    content: String,
    sourceContent: String = content,
    modifier: Modifier = Modifier,
    selectable: Boolean = false,
    // Let callers/MaterialTheme control font size so in-app text size settings work.
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    baseUrl: String = Constants.baseUrl,
    linkFloor: Boolean = false,
    onUriClick: ((uri: String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    loadImage: ((html: String, img: String?) -> Unit)? = null,
    onHtmlImageClick: ((String, List<String>) -> Unit)? = null,
) {
    val currentSourceContent = rememberUpdatedState(sourceContent)
    val currentLoadImage = rememberUpdatedState(loadImage)

    HtmlText(
        content,
        modifier = modifier,
        selectable = selectable,
        textStyle = textStyle,
        baseUrl = baseUrl,
        onLinkClick = onUriClick,
        onClick = onClick,
        loadImage = { src: String -> currentLoadImage.value?.invoke(currentSourceContent.value, src) },
        onImageClick = { clicked: Img, all: List<Img> ->
            onHtmlImageClick?.invoke(clicked.src, all.map(Img::src))
        },
    )

    LaunchedEffect(sourceContent) {
        currentLoadImage.value?.invoke(sourceContent, null)
    }
}
