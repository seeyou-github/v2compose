package io.github.v2compose.ui.write

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation
import io.ktor.http.encodeURLParameter

internal const val argsNode = "node"
internal const val argsNodeTitle = "node_title"
const val createTopicNavigationRoute =
    "/write?node={node}&node_title={node_title}"

fun NavController.navigateToWriteTopic(node: String? = null, nodeTitle: String? = null) {
    val encodedNode = node?.encodeURLParameter() ?: ""
    val encodedNodeTitle = nodeTitle?.encodeURLParameter() ?: ""
    navigate("/write?node=$encodedNode&node_title=$encodedNodeTitle")
}

fun NavGraphBuilder.writeTopicScreen(
    onCloseClick: () -> Unit,
    openUri: (String) -> Unit,
    onCreateTopicSuccess: (topicId: String) -> Unit,
) {
    composableWithAnimation(
        route = createTopicNavigationRoute,
        arguments = listOf(
            navArgument(argsNode) { type = NavType.StringType },
            navArgument(argsNodeTitle) { type = NavType.StringType },
        )
    ) {
        WriteTopicScreenRoute(
            onCloseClick = onCloseClick,
            openUri = openUri,
            onCreateTopicSuccess = onCreateTopicSuccess
        )
    }
}
