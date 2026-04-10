package io.github.v2compose.ui.node

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation
import io.github.v2compose.network.bean.NodeTopicInfo

fun NavGraphBuilder.nodeScreen(
    onBackClick: () -> Unit,
    onTopicClick: (NodeTopicInfo.Item) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
) {
    composableWithAnimation(
        route = nodeNavigationNavigationRoute,
        arguments = listOf(
            navArgument(nodeArgsNodeName) { type = NavType.StringType },
            navArgument(nodeArgsNodeTitle) {
                type = NavType.StringType
                nullable = true
            })
    ) {
        NodeRoute(
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
            onUserAvatarClick = onUserAvatarClick,
            openUri = openUri,
        )
    }
}
