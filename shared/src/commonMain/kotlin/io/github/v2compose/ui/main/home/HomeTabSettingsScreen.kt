package io.github.v2compose.ui.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.shared.bean.TopicNode
import io.github.v2compose.ui.common.ListDivider
import io.github.v2compose.ui.common.SelectNode
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeTabSettingsScreenRoute(
    onBackClick: () -> Unit,
    viewModel: HomeTabSettingsViewModel = koinViewModel(),
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val initialTabs = remember(appSettings.homeTabConfigsJson) {
        val decoded = HomeTabConfig.decodeList(appSettings.homeTabConfigsJson)
        (if (decoded.isEmpty()) HomeTabConfig.defaultTabs() else decoded)
    }
    HomeTabSettingsScreen(
        tabs = initialTabs,
        onBackClick = onBackClick,
        onTabsChanged = viewModel::saveHomeTabs,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTabSettingsScreen(
    tabs: List<HomeTabConfig>,
    onBackClick: () -> Unit,
    onTabsChanged: (List<HomeTabConfig>) -> Unit,
) {
    var currentTabs by remember(tabs) { mutableStateOf(tabs) }
    val coroutineScope = rememberCoroutineScope()
    val topicRepository: TopicRepository = koinInject()

    var showSelectNode by remember { mutableStateOf(false) }
    var availableNodes by remember { mutableStateOf<List<TopicNode>>(emptyList()) }

    fun updateTabs(newTabs: List<HomeTabConfig>) {
        currentTabs = newTabs
        onTabsChanged(newTabs)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showSelectNode = true
                    if (availableNodes.isEmpty()) {
                        coroutineScope.launch {
                            runCatching {
                                availableNodes = topicRepository.getTopicNodes()
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "add")
            }
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    items = currentTabs,
                    key = { _, item -> item.id },
                ) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                            val subtitle = when {
                                item.isNodeTab() -> "节点: ${item.nodeName}"
                                else -> "内置: ${item.newsTabValue}"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        IconButton(
                            enabled = index > 0,
                            onClick = {
                                val list = currentTabs.toMutableList()
                                val tmp = list[index - 1]
                                list[index - 1] = list[index]
                                list[index] = tmp
                                updateTabs(list)
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "up")
                        }
                        IconButton(
                            enabled = index < currentTabs.lastIndex,
                            onClick = {
                                val list = currentTabs.toMutableList()
                                val tmp = list[index + 1]
                                list[index + 1] = list[index]
                                list[index] = tmp
                                updateTabs(list)
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "down")
                        }

                        if (item.isNodeTab()) {
                            IconButton(
                                onClick = {
                                    updateTabs(currentTabs.filterNot { it.id == item.id })
                                },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "delete")
                            }
                        }

                        Switch(
                            checked = item.enabled,
                            onCheckedChange = { enabled ->
                                updateTabs(
                                    currentTabs.map {
                                        if (it.id == item.id) it.copy(enabled = enabled) else it
                                    }
                                )
                            },
                        )
                    }
                    ListDivider()
                }

                item(key = "hint") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "提示: 右下角 + 用于新增节点分类",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showSelectNode) {
        SelectNode(
            nodes = availableNodes,
            onNodeClick = { node ->
                showSelectNode = false
                val id = "node:${node.name}"
                val existing = currentTabs.indexOfFirst { it.id == id }
                val updated = if (existing >= 0) {
                    currentTabs.map {
                        if (it.id == id) it.copy(name = node.title, enabled = true) else it
                    }
                } else {
                    currentTabs + HomeTabConfig(
                        id = id,
                        name = node.title,
                        nodeName = node.name,
                        enabled = true,
                    )
                }
                updateTabs(updated)
            },
            onDismiss = { showSelectNode = false },
        )
    }
}
