package io.github.v2compose.ui.supplement

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v2compose.R
import io.github.v2compose.shared.bean.ContentFormat
import io.github.v2compose.network.bean.AppendTopicPageInfo
import io.github.v2compose.ui.common.CloseButton
import io.github.v2compose.ui.common.HtmlAlertDialog
import io.github.v2compose.ui.common.TextAlertDialog
import io.github.v2compose.ui.common.TextEditor

@Composable
fun AddSupplementScreenRoute(
    onCloseClick: () -> Unit,
    onAddSupplementSuccess: (String) -> Unit,
    openUri: (String) -> Unit,
    viewModel: AddSupplementViewModel = koinViewModel(),
    screenState: AddSupplementScreenState = rememberAddSupplementScreenState(),
) {
    val topicId = viewModel.args.topicId
    val pageInfo by viewModel.pageInfo.collectAsStateWithLifecycle()
    val addSupplementState by viewModel.addSupplementState.collectAsStateWithLifecycle()

    HandleProblem(pageInfo, openUri)

    HandleAddSupplementState(screenState, addSupplementState, onAddSupplementSuccess, topicId)

    AddSupplementScreen(
        addSupplementState = addSupplementState,
        onCloseClick = onCloseClick,
        onAddSupplementClick = viewModel::addSupplement,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSupplementScreen(
    addSupplementState: AddSupplementState,
    onCloseClick: () -> Unit,
    onAddSupplementClick: (String, ContentFormat) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var supplement by rememberSaveable { mutableStateOf("") }
    var contentFormat by rememberSaveable { mutableStateOf(ContentFormat.Original) }

    AddSupplementBackHandler(supplement, onCloseClick)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { CloseButton(onClick = onCloseClick) },
                title = { Text(stringResource(id = R.string.add_supplement)) },
                actions = {
                    AddSupplementButton(addSupplementState) {
                        onAddSupplementClick(supplement, contentFormat)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime),
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
//            WriteSupplementField(
//                supplement,
//                onTextChanged = { supplement = it },
//                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
//            )
            TextEditor(
                content = supplement,
                placeholder = stringResource(id = R.string.add_supplement_tips),
                contentFormat = contentFormat,
                onContentChanged = { supplement = it },
                onContentFormatChanged = { contentFormat = it },
                contentFocusRequester = focusRequester
            )
        }
    }

    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun AddSupplementButton(
    addSupplementState: AddSupplementState,
    onAddSupplementClick: () -> Unit
) {
    IconButton(onClick = { onAddSupplementClick() }) {
        if (addSupplementState is AddSupplementState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.AutoMirrored.Rounded.Send, "send")
        }
    }
}

@Composable
fun WriteSupplementField(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    TextField(
        value = text,
        onValueChange = onTextChanged,
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
        placeholder = {
            Text(
                stringResource(id = R.string.add_supplement_tips),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun AddSupplementBackHandler(supplement: String, onCloseClick: () -> Unit) {
    var showBackTips by remember { mutableStateOf(false) }

    BackHandler(enabled = supplement.isNotEmpty()) {
        showBackTips = true
    }

    if (showBackTips) {
        TextAlertDialog(
            message = stringResource(id = R.string.add_supplement_back_tips),
            onConfirm = { onCloseClick() },
            onDismiss = { showBackTips = false })
    }
}

@Composable
private fun HandleProblem(pageInfo: AppendTopicPageInfo?, onUriClick: (String) -> Unit) {
    pageInfo?.problem?.let {
        if (!it.isEmpty()) {
            HtmlAlertDialog(content = it.html, onUriClick = onUriClick)
        }
    }
}

@Composable
private fun HandleAddSupplementState(
    screenState: AddSupplementScreenState,
    addSupplementState: AddSupplementState,
    onAddSupplementSuccess: (String) -> Unit,
    topicId: String
) {
    if (addSupplementState is AddSupplementState.Success) {
        LaunchedEffect(addSupplementState) {
            onAddSupplementSuccess(topicId)
        }
    } else if (addSupplementState is AddSupplementState.Error) {
        LaunchedEffect(addSupplementState) {
            addSupplementState.error?.message.let {
                if (it.isNullOrEmpty()) {
                    screenState.showMessage(R.string.add_supplement_fail_tips)
                } else {
                    screenState.showMessage(it)
                }
            }
        }
    }
}

