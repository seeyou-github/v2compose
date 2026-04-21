package io.github.v2compose.ui.write

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Polyline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.network.bean.CreateTopicPageInfo
import io.github.v2compose.shared.bean.ContentFormat
import io.github.v2compose.shared.bean.DraftTopic
import io.github.v2compose.shared.bean.TopicNode
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.common.HtmlAlertDialog
import io.github.v2compose.ui.common.ListDivider
import io.github.v2compose.ui.common.PlatformBackHandler
import io.github.v2compose.ui.common.SelectNode
import io.github.v2compose.ui.common.TextEditor
import io.github.v2compose.usecase.LoadNodesState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.create_topic
import v2compose.shared.generated.resources.load_nodes_failure
import v2compose.shared.generated.resources.select_node
import v2compose.shared.generated.resources.topic_content_placeholder
import v2compose.shared.generated.resources.topic_title

private val TopicNodeSaver: Saver<TopicNode?, Any> = listSaver(
    save = { node -> listOf(node?.name, node?.title, node?.topics, node?.aliases) },
    restore = { values ->
        val name = values.getOrNull(0) as? String ?: return@listSaver null
        val title = values.getOrNull(1) as? String ?: return@listSaver null
        val topics = values.getOrNull(2) as? Int ?: return@listSaver null
        val aliases = (values.getOrNull(3) as? List<*>)?.filterIsInstance<String>().orEmpty()

        TopicNode(
            name = name,
            title = title,
            topics = topics,
            aliases = aliases,
        )
    }
)

@Composable
fun WriteTopicScreenRoute(
    onCloseClick: () -> Unit,
    openUri: (String) -> Unit,
    onCreateTopicSuccess: (topicId: String) -> Unit,
    viewModel: WriteTopicViewModel = koinViewModel(),
    screenState: WriteTopicScreenState = rememberWriteTopicScreenState(),
) {
    val loadNodesState by viewModel.loadNodes.state.collectAsStateWithLifecycle()
    val createTopicState by viewModel.createTopicState.collectAsStateWithLifecycle()
    val draftTopicUiState by viewModel.draftTopicUiState.collectAsStateWithLifecycle()

    HandleLoadNodesState(loadNodesState, screenState)

    HandleCreateTopicState(
        createTopicState = createTopicState,
        screenState = screenState,
        onUriClick = openUri,
        onCreateTopicSuccess = onCreateTopicSuccess
    )

    WriteTopicScreen(
        initialDraftTopic = draftTopicUiState.draftTopic,
        draftLoaded = draftTopicUiState.isLoaded,
        loadNodesState = loadNodesState,
        createTopicState = createTopicState,
        snackbarHostState = screenState.snackbarHostState,
        onCloseClick = onCloseClick,
        onTopicChanged = viewModel::saveDraftTopic,
        onSendClick = { title, content, contentFormat, node ->
            if (screenState.check(title, content, node)) {
                viewModel.createTopic(title, content.trim(), contentFormat, node!!.name)
            }
        },
        retryLoadingNodes = viewModel::loadNodes,
    )
}

@Composable
private fun WriteTopicScreen(
    initialDraftTopic: DraftTopic,
    draftLoaded: Boolean,
    createTopicState: CreateTopicState,
    loadNodesState: LoadNodesState,
    snackbarHostState: SnackbarHostState,
    onCloseClick: () -> Unit,
    onTopicChanged: (String, String, ContentFormat, TopicNode?) -> Unit,
    onSendClick: (title: String, content: String, contentFormat: ContentFormat, node: TopicNode?) -> Unit,
    retryLoadingNodes: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initialDraftTopic.title) }
    var content by rememberSaveable { mutableStateOf(initialDraftTopic.content) }
    var contentFormat by rememberSaveable { mutableStateOf(initialDraftTopic.contentFormat) }
    var node by rememberSaveable(stateSaver = TopicNodeSaver) { mutableStateOf(initialDraftTopic.node) }
    var hasUserEditedDraft by rememberSaveable { mutableStateOf(false) }

    var showNodes by remember { mutableStateOf(false) }
    val successLoadNodesState = loadNodesState as? LoadNodesState.Success

    LaunchedEffect(draftLoaded, initialDraftTopic) {
        if (draftLoaded && !hasUserEditedDraft) {
            title = initialDraftTopic.title
            content = initialDraftTopic.content
            contentFormat = initialDraftTopic.contentFormat
            node = initialDraftTopic.node
        }
    }

    PlatformBackHandler(enabled = showNodes) {
        showNodes = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBar(
                    createTopicState = createTopicState,
                    onCloseClick = onCloseClick,
                    onSendClick = { onSendClick(title, content, contentFormat, node) },
                )
            },
            contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { insets ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(insets)
            ) {
                Column {
                    val titleFocusRequester = remember { FocusRequester() }
                    val contentFocusRequester = remember { FocusRequester() }

                    TopicTitleField(
                        title = title,
                        onTitleChanged = {
                            hasUserEditedDraft = true
                            title = it
                            onTopicChanged(title, content, contentFormat, node)
                        },
                        onNextAction = { contentFocusRequester.requestFocus() },
                        modifier = Modifier.focusRequester(titleFocusRequester),
                    )

                    ListDivider()

                    Box(modifier = Modifier.weight(1f)) {
                        TextEditor(
                            content = content,
                            placeholder = stringResource(Res.string.topic_content_placeholder),
                            contentFormat = contentFormat,
                            onContentChanged = {
                                hasUserEditedDraft = true
                                content = it
                                onTopicChanged(title, content, contentFormat, node)
                            },
                            onContentFormatChanged = {
                                hasUserEditedDraft = true
                                contentFormat = it
                                onTopicChanged(title, content, contentFormat, node)
                            },
                            contentFocusRequester = contentFocusRequester,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 40.dp),
                        )

                        TopicNodeField(
                            loadNodesState = loadNodesState,
                            node = node,
                            onNodeClick = {
                                showNodes = true
                                if (loadNodesState !is LoadNodesState.Success) {
                                    retryLoadingNodes()
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    LaunchedEffect(showNodes) {
                        if (!showNodes) {
                            titleFocusRequester.requestFocus()
                        }
                    }
                }
            }
        }

        if (showNodes && successLoadNodesState?.data?.isNotEmpty() == true) {
            val nodes = successLoadNodesState.data
            SelectNode(
                nodes = nodes,
                onNodeClick = {
                    hasUserEditedDraft = true
                    showNodes = false
                    node = it
                    onTopicChanged(title, content, contentFormat, node)
                },
                onDismiss = { showNodes = false },
            )
        }
    }

}

@Composable
private fun TopicTitleField(
    title: String,
    onTitleChanged: (String) -> Unit,
    onNextAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(title, TextRange(title.length)))
    }
    LaunchedEffect(title) {
        if (textFieldValue.text != title) {
            textFieldValue = TextFieldValue(title, TextRange(title.length))
        }
    }

    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onTitleChanged(it.text)
        },
        modifier = modifier.fillMaxWidth(),
//        modifier = modifier
//            .fillMaxWidth()
//            .heightIn(min = 80.dp),
        keyboardActions = KeyboardActions(onNext = { onNextAction() }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = TextFieldDefaults.colors(
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            errorPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = placeholderColor,
        ),
        placeholder = { Text(stringResource(Res.string.topic_title)) },
        textStyle = MaterialTheme.typography.bodyLarge,
    )

}


@Composable
fun TopicContentField(
    content: String, onContentChanged: (String) -> Unit, modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }
    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = TextFieldValue(content, TextRange(content.length))
        }
    }
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant

    TextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onContentChanged(it.text)
        },
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedPlaceholderColor = placeholderColor,
            unfocusedPlaceholderColor = placeholderColor,
            errorPlaceholderColor = placeholderColor,
            disabledPlaceholderColor = placeholderColor,
        ),
        placeholder = { Text(stringResource(Res.string.topic_content_placeholder)) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.3.sp,
        ),
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    createTopicState: CreateTopicState, onCloseClick: () -> Unit, onSendClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(Res.string.create_topic)) },
        navigationIcon = { CloseButton(onClick = onCloseClick) },
        actions = {
            SendButton(
                inProgress = createTopicState is CreateTopicState.Loading,
                onClick = onSendClick,
            )
        },
    )
}

@Composable
private fun SendButton(
    inProgress: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        if (inProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp
            )
        } else {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "send")
        }
    }
}


@Composable
private fun HandleLoadNodesState(
    loadNodesState: LoadNodesState, screenState: WriteTopicScreenState
) {
    if (loadNodesState is LoadNodesState.Error) {
        val defaultMessage = stringResource(Res.string.load_nodes_failure)
        LaunchedEffect(loadNodesState) {
            val message = loadNodesState.error?.message
            if (message != null) {
                screenState.showMessage(message)
            } else {
                screenState.showMessage(defaultMessage)
            }
        }
    }
}

@Composable
private fun HandleCreateTopicState(
    createTopicState: CreateTopicState,
    screenState: WriteTopicScreenState,
    onUriClick: (String) -> Unit,
    onCreateTopicSuccess: (topicId: String) -> Unit,
) {
    when (createTopicState) {
        is CreateTopicState.Error -> {
            val defaultMessage = stringResource(Res.string.load_nodes_failure)
            LaunchedEffect(createTopicState) {
                val message = createTopicState.error?.message
                if (message != null) {
                    screenState.showMessage(message)
                } else {
                    screenState.showMessage(defaultMessage)
                }
            }
        }

        is CreateTopicState.Failure -> {
            val problem: CreateTopicPageInfo.Problem = createTopicState.pageInfo.problem ?: return
            if (problem.isEmpty()) return
            var showProblem by remember(createTopicState) { mutableStateOf(true) }
            if (showProblem) {
                HtmlAlertDialog(
                    content = problem.html,
                    onUriClick = onUriClick,
                    onDismissRequest = { showProblem = false },
                )
            }
        }

        is CreateTopicState.Success -> {
            LaunchedEffect(createTopicState) {
                onCreateTopicSuccess(createTopicState.topicId)
            }
        }

        else -> {}
    }
}

@Composable
private fun TopicNodeField(
    loadNodesState: LoadNodesState,
    node: TopicNode?,
    onNodeClick: (TopicNode?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(40.dp)
                .widthIn(min = 108.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onNodeClick(node) },
        ) {
            val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            val nodeTitle = node?.title
            Spacer(Modifier.width(12.dp))
            if (loadNodesState is LoadNodesState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = contentColor, strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Rounded.Polyline, contentDescription = "node", tint = contentColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (nodeTitle.isNullOrEmpty()) {
                    stringResource(Res.string.select_node)
                } else {
                    nodeTitle
                },
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(12.dp))
        }
    }
}
