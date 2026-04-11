package io.github.v2compose.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.ok

@Composable
fun HtmlAlertDialog(
    title: String? = null,
    content: String,
    onUriClick: ((uri: String) -> Unit)? = null,
) {
    var showDialog by remember(content) { mutableStateOf(content.isNotEmpty()) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { title?.let { Text(it) } },
            text = { HtmlContent(content = content, onUriClick = onUriClick) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.ok))
                }
            },
        )
    }
}
