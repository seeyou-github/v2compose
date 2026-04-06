package io.github.cooaer.htmltext

import androidx.compose.runtime.Composable

@Composable
expect fun YouTubePlayer(videoId: String)

expect fun String.parseYouTubeVideoId(): String?

expect fun logDebug(tag: String, message: String)

expect fun String.isImageUrl(): Boolean