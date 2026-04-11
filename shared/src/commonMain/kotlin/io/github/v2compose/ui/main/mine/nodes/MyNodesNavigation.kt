package io.github.v2compose.ui.main.mine.nodes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import io.github.v2compose.core.composableWithAnimation
import io.github.v2compose.network.bean.MyNodesInfo

const val myNodesRoute = "/my/nodes"

fun NavController.navigateToMyNodes() {
    navigate(myNodesRoute)
}

fun NavGraphBuilder.myNodesScreen(
    onBackClick: () -> Unit,
    onNodeClick: (MyNodesInfo.Item) -> Unit
) {
    composableWithAnimation(myNodesRoute) {
        MyNodesScreenRoute(onBackClick = onBackClick, onNodeClick = onNodeClick)
    }
}