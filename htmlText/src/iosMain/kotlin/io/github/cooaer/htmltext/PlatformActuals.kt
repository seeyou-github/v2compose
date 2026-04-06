package io.github.cooaer.htmltext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun YouTubePlayer(videoId: String) {
    // Placeholder for iOS
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("YouTube Player (iOS placeholder): $videoId")
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