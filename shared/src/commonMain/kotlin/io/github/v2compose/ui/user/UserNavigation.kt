package io.github.v2compose.ui.user

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.composableWithAnimation
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPathPart

private const val argsUserName = "userName"
private const val argsAvatar = "userAvatar"

const val userScreenNavigationRoute = "/member/{$argsUserName}?userAvatar={$argsAvatar}"

data class UserArgs(val userName: String, val avatar: String? = null) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) : this(
        stringDecoder.decodeString(checkNotNull(savedStateHandle[argsUserName])),
        savedStateHandle.get<String>(argsAvatar)?.let { stringDecoder.decodeString(it) },
    )
}

fun NavController.navigateToUser(userName: String, userAvatar: String? = null) {
    val encodedUserName = userName.encodeURLPathPart()
    val encodedUserAvatar = userAvatar?.encodeURLParameter().orEmpty()
    navigate("/member/$encodedUserName?userAvatar=$encodedUserAvatar")
}

fun NavGraphBuilder.userScreen(
    onBackClick: () -> Unit,
    onTopicClick: (String) -> Unit,
    onNodeClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    onShareUser: (String, String) -> Unit,
) {
    composableWithAnimation(
        route = userScreenNavigationRoute,
        arguments = listOf(
            navArgument(argsUserName) { type = NavType.StringType },
            navArgument(argsAvatar) {
                type = NavType.StringType
                nullable = true
            },
        )
    ) {
        UserScreenRoute(
            onBackClick = onBackClick,
            onTopicClick = onTopicClick,
            onNodeClick = onNodeClick,
            openUri = openUri,
            onHtmlImageClick = onHtmlImageClick,
            onShareUser = onShareUser,
        )
    }
}
