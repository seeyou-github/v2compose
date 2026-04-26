package io.github.cooaer.htmltext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun YouTubePlayer(videoId: String, onOpenExternalUri: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "iOS v1 暂不支持内嵌 YouTube 播放",
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = {
                    onOpenExternalUri("https://www.youtube.com/watch?v=$videoId")
                },
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text("在浏览器打开")
            }
        }
    }
}

actual fun String.parseYouTubeVideoId(): String? {
    // Basic string parsing for YouTube embed URL: /embed/videoId
    val parts = this.split("/")
    val embedIndex = parts.indexOf("embed")
    if (embedIndex != -1 && embedIndex + 1 < parts.size) {
        return parts[embedIndex + 1]
    }
    return null
}

actual fun logDebug(tag: String, message: String) {
    println("[$tag] $message")
}

private val imageFormats = listOf("png", "jpg", "jpeg", "webp", "gif")

actual fun String.isImageUrl(): Boolean {
    val lowerPath = this.substringBefore("?").lowercase()
    return imageFormats.any { lowerPath.endsWith(".$it") }
}
