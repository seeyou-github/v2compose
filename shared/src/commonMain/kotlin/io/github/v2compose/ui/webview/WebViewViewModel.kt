package io.github.v2compose.ui.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.v2compose.core.StringDecoder

class WebViewViewModel(
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder,
) : ViewModel() {
    val url = stringDecoder.decodeString(savedStateHandle.get<String>(argsUrl).orEmpty())
}
