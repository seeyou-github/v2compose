package io.github.v2compose.ui.write

import androidx.lifecycle.SavedStateHandle
import io.github.v2compose.core.StringDecoder

private const val argsNode = "node"
private const val argsNodeTitle = "node_title"

data class WriteTopicArgs(val nodeName: String?, val nodeTitle: String?) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) : this(
        savedStateHandle.get<String>(argsNode)?.let { stringDecoder.decodeString(it) },
        savedStateHandle.get<String>(argsNodeTitle)?.let { stringDecoder.decodeString(it) },
    )
}
