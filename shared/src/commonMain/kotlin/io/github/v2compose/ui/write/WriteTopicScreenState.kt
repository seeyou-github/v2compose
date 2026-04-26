package io.github.v2compose.ui.write


import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.v2compose.shared.bean.TopicNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.node_empty
import v2compose.shared.generated.resources.topic_title_empty

@Composable
fun rememberWriteTopicScreenState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
): WriteTopicScreenState {
    return remember(coroutineScope, snackbarHostState) {
        WriteTopicScreenState(coroutineScope, snackbarHostState)
    }
}

@Stable
class WriteTopicScreenState(
    private val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {

    fun check(title: String, content: String, node: TopicNode?): Boolean {
        if (title.isEmpty()) {
            coroutineScope.launch { showMessage(getString(Res.string.topic_title_empty)) }
            return false
        }
        if (node?.name.isNullOrEmpty()) {
            coroutineScope.launch { showMessage(getString(Res.string.node_empty)) }
            return false
        }
        return true
    }


    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }


}