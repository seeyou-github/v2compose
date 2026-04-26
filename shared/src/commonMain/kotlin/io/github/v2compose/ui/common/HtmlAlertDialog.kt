package io.github.v2compose.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.ok

@Composable
fun HtmlAlertDialog(
    title: String? = null,
    content: String,
    onUriClick: ((uri: String) -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {
    if (content.isEmpty()) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { title?.let { Text(it) } },
        text = { HtmlContent(content = content, onUriClick = onUriClick) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.ok))
            }
        },
    )
}
