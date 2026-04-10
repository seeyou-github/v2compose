package io.github.v2compose.ui.search

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.v2compose.network.bean.SoV2EXSearchResultInfo

fun NavGraphBuilder.searchScreen(
    goBack: () -> Unit,
    onTopicClick: (SoV2EXSearchResultInfo.Hit) -> Unit
) {
    composable(
        route = searchScreenNavigationRoute,
        arguments = listOf(navArgument(searchArgsKeyword) {
            type = NavType.StringType
            nullable = true
        })
    ) {
        SearchScreenRoute(goBack = goBack, onTopicClick = onTopicClick)
    }
}
