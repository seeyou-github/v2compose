package io.github.v2compose.ui.topic.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.reply

private val fabSize = 56.dp
private val borderPadding = 16.dp

val fabSizeWithMargin = fabSize + borderPadding * 2
val fabsVerticalSpace = 8.dp
val replyInputHeight = fabSize * 2 + fabsVerticalSpace

private const val TAG = "CreateReply"

enum class ReplyInputState {
    Collapsed, Expanded,
}

@Composable
fun ReplyInput(
    initialValue: String,
    clickReplyTimes: Int,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: ReplyInputState = ReplyInputState.Collapsed,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var textFieldValueState by remember(initialValue) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = TextRange(initialValue.length)
            )
        )
    }

    val boxPaddingEnd by animateDpAsState(
        targetValue = if (state == ReplyInputState.Expanded) {
            fabSize + borderPadding * 2
        } else {
            borderPadding
        }
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = borderPadding,
                top = borderPadding,
                end = boxPaddingEnd,
                bottom = borderPadding
            )
    ) {
        val expandedWidthPx = with(LocalDensity.current) {
            (maxWidth - fabSize - borderPadding * 3).roundToPx()
        }
        AnimatedVisibility(
            visible = state == ReplyInputState.Expanded,
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            OutlinedTextField(
                value = textFieldValueState,
                onValueChange = {
                    textFieldValueState = it
                    onValueChanged(it.text)
                },
                placeholder = { Text(text = stringResource(Res.string.reply)) },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .align(Alignment.BottomEnd)
                    .animateContentSize(finishedListener = { initialValue, targetValue ->
                        val keyboardVisible = targetValue.width >= expandedWidthPx
                        if (keyboardVisible) {
                            focusRequester.requestFocus()
                            keyboard?.show()
                        } else {
                            focusRequester.freeFocus()
                            keyboard?.hide()
                        }
                    })
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(fabSize / 2)
                    ).fillMaxWidth().height(replyInputHeight),
                shape = RoundedCornerShape(fabSize / 2)
            )

            LaunchedEffect(clickReplyTimes) {
                focusRequester.requestFocus()
                keyboard?.show()
            }
        }
    }
}
