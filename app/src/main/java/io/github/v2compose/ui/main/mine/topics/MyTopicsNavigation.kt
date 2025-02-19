package io.github.v2compose.ui.main.mine.topics

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.v2compose.network.bean.MyTopicsInfo

const val myTopicsRoute = "/my/topics"

fun NavController.navigateToMyTopics() {
    navigate(myTopicsRoute)
}

fun NavGraphBuilder.myTopicsScreen(
    onBackClick: () -> Unit,
    onTopicClick: (MyTopicsInfo.Item) -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
) {
    composable(myTopicsRoute) {
        MyTopicsScreenRoute(
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
            onNodeClick = onNodeClick,
            onUserAvatarClick = onUserAvatarClick,
        )
    }
}