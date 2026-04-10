package io.github.v2compose.ui.node

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import io.github.v2compose.core.StringDecoder
import io.ktor.http.encodeURLParameter

const val nodeArgsNodeName = "nodeName"
const val nodeArgsNodeTitle = "nodeTitle"

const val nodeNavigationNavigationRoute =
    "/go/{$nodeArgsNodeName}?$nodeArgsNodeTitle={$nodeArgsNodeTitle}"

data class NodeArgs(val nodeName: String, val nodeTitle: String? = null) {
    constructor(
        savedStateHandle: SavedStateHandle,
        stringDecoder: StringDecoder,
    ) : this(
        nodeName = stringDecoder.decodeString(checkNotNull(savedStateHandle[nodeArgsNodeName])),
        nodeTitle = savedStateHandle.get<String>(nodeArgsNodeTitle)
            ?.let(stringDecoder::decodeString),
    )
}

fun NavController.navigateToNode(nodeName: String, nodeTitle: String? = null) {
    val encodedNodeName = nodeName.encodeURLParameter()
    val encodedNodeTitle = nodeTitle?.encodeURLParameter().orEmpty()
    navigate("/go/$encodedNodeName?$nodeArgsNodeTitle=$encodedNodeTitle")
}
