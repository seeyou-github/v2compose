package io.github.v2compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import io.github.v2compose.core.openInBrowser
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.save_image_failed
import v2compose.shared.generated.resources.save_image_success
import java.io.File

@Composable
fun rememberAndroidExternalUriHandler(
    context: Context = LocalContext.current,
): (String) -> Unit = remember(context) {
    { uri -> context.openExternalUri(uri) }
}

@Composable
fun rememberAndroidImageSaver(
    snackbarHostState: SnackbarHostState,
    context: Context = LocalContext.current,
): (String) -> Unit {
    val coroutineScope = rememberCoroutineScope()
    return remember(context, coroutineScope, snackbarHostState) {
        { url ->
            coroutineScope.launch {
                context.saveImage(url, snackbarHostState)
            }
        }
    }
}

private fun Context.openExternalUri(uri: String) {
    val parsedUri = runCatching { Uri.parse(uri) }.getOrNull() ?: return
    when (parsedUri.scheme) {
        "mailto", "sms", "tel" -> startActivity(Intent(Intent.ACTION_VIEW, parsedUri))
        else -> openInBrowser(uri, true)
    }
}

@OptIn(ExperimentalCoilApi::class)
private suspend fun Context.saveImage(
    url: String,
    snackbarHostState: SnackbarHostState,
) {
    val imageName = Uri.parse(url).lastPathSegment ?: return
    val pictureDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val appImageDir = File(pictureDir, "v2compose").also { it.mkdirs() }

    val message = imageLoader.diskCache?.openSnapshot(url)?.let { snapshot ->
        val newFile = File(appImageDir, imageName)
        snapshot.data.toFile().copyTo(newFile, overwrite = true)
        snapshot.close()
        getString(Res.string.save_image_success)
    } ?: getString(Res.string.save_image_failed)

    snackbarHostState.showSnackbar(
        message = message,
        duration = SnackbarDuration.Short,
    )
}
