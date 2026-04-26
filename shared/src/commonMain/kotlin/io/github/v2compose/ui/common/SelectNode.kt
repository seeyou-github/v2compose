package io.github.v2compose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.v2compose.shared.bean.TopicNode
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.search_all_nodes

@Composable
fun SelectNode(
    nodes: List<TopicNode>,
    onNodeClick: (TopicNode) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    var searchKey by remember { mutableStateOf("") }
    var currentNodes by remember(searchKey) { mutableStateOf(nodes) }

    LaunchedEffect(nodes, searchKey) {
        currentNodes = if (searchKey.isEmpty()) nodes else {
            nodes.filter { node ->
                node.title.contains(searchKey, true) || node.name.contains(
                    searchKey, true
                ) || node.aliases.any { it.contains(searchKey, true) }
            }
        }
    }

    Surface(
        color = Color.Transparent,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .clickable { onDismiss() }
                .background(color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.6f))
                .systemBarsPadding()
                .imePadding()
                .padding(32.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 16.dp)
            ) {
                Column {
                    Box {
                        TextField(
                            value = searchKey,
                            onValueChange = { searchKey = it },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = { Text(text = stringResource(Res.string.search_all_nodes)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            )
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "close")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        itemsIndexed(
                            items = currentNodes,
                            key = { _, item -> item.name }) { _, item ->
                            NodeListItem(item, onNodeClick)
                        }
                    }

                    LaunchedEffect(true) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeListItem(
    item: TopicNode, onNodeClick: (TopicNode) -> Unit
) {
    Text(
        "${item.title} / ${item.name}",
        modifier = Modifier
            .clickable { onNodeClick(item) }
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp)
            .wrapContentHeight(align = Alignment.CenterVertically),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}