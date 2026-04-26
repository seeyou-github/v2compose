package io.github.cooaer.htmltext

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer as NativeYouTubePlayer

private const val TAG = "VideoPlayer"

@Composable
actual fun YouTubePlayer(videoId: String, onOpenExternalUri: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var startSeconds by rememberSaveable { mutableFloatStateOf(0f) }

    Log.d(TAG, "YouTubePlayer, videoId = $videoId")

    val youtubePlayer = remember(context, videoId) {
        YouTubePlayerView(context).apply {
            enableAutomaticInitialization = false
            initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: NativeYouTubePlayer) {
                    youTubePlayer.cueVideo(videoId, startSeconds)
                }

                override fun onCurrentSecond(youTubePlayer: NativeYouTubePlayer, second: Float) {
                    startSeconds = second
                }

                override fun onStateChange(
                    youTubePlayer: NativeYouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    Log.d(TAG, "YouTubePlayer, videoId = $videoId, state = $state")
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, youtubePlayer) {
        lifecycleOwner.lifecycle.addObserver(youtubePlayer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(youtubePlayer)
        }
    }

    AndroidView(
        factory = { youtubePlayer },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    )
}

actual fun String.parseYouTubeVideoId(): String? {
    return toUri().pathSegments.let {
        if (it.getOrNull(0)?.lowercase() == "embed") it.getOrNull(1) else null
    }
}

actual fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

private val imageFormats = listOf("png", "jpg", "jpeg", "webp", "gif")

actual fun String.isImageUrl(): Boolean {
    return Uri.parse(this)?.let { uri ->
        imageFormats.any { format -> uri.lastPathSegment?.endsWith(".$format", true) == true }
    } ?: false
}
