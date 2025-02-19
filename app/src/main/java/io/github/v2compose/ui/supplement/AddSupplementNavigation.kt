package io.github.v2compose.ui.supplement

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation

private const val argsTopicId = "topicId"
const val addSupplementNavigationRoute = "/append/topic/{$argsTopicId}"

class AddSupplementArgs(val topicId: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        checkNotNull(
            savedStateHandle.get<String>(argsTopicId)
        )
    )
}

fun NavController.navigateToAddSupplement(topicId: String) {
    navigate("/append/topic/$topicId")
}

fun NavGraphBuilder.addSupplementScreen(
    onCloseClick: () -> Unit,
    onAddSupplementSuccess: (String) -> Unit,
    openUri: (String) -> Unit,
) {
    composableWithAnimation(
        addSupplementNavigationRoute,
        arguments = listOf(navArgument(argsTopicId) { type = NavType.StringType })
    ) {
        AddSupplementScreenRoute(
            onCloseClick = onCloseClick,
            onAddSupplementSuccess = onAddSupplementSuccess,
            openUri = openUri
        )
    }
}

