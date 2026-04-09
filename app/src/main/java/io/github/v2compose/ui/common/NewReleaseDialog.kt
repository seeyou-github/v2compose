package io.github.v2compose.ui.common


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import io.github.v2compose.network.bean.Release
import v2compose.shared.generated.resources.*


@Composable
fun NewReleaseDialog(
    release: Release,
    onIgnoreClick: () -> Unit,
    onCancelClick: () -> Unit,
    onOkClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelClick,
        confirmButton = { TextButton(onClick = onOkClick) { Text(stringResource(Res.string.goto_update)) } },
        title = { Text(stringResource(Res.string.has_new_updates)) },
        text = { NewReleaseBody(release.body ?: release.name ?: release.tagName) },
        dismissButton = {
            TextButton(onClick = onIgnoreClick) {
                Text(stringResource(Res.string.ignore_this_release))
            }
        }
    )
}

@Composable
private fun NewReleaseBody(text: String) {
    Markdown(
        content = text,
        modifier = Modifier.fillMaxWidth(),
        imageTransformer = Coil3ImageTransformerImpl
    )
}