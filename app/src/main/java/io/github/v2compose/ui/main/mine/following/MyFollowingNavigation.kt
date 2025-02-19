package io.github.v2compose.ui.main.mine.following

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.v2compose.network.bean.MyFollowingInfo

const val myFollowingRoute = "/my/following"

fun NavController.navigateToMyFollowing() {
    navigate(myFollowingRoute)
}

fun NavGraphBuilder.myFollowingScreen(
    onBackClick: () -> Unit,
    onTopicClick: (MyFollowingInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    composable(myFollowingRoute) {
        MyFollowingScreenRoute(
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
            onNodeClick = onNodeClick,
            onUserAvatarClick = onUserAvatarClick,
        )
    }
}