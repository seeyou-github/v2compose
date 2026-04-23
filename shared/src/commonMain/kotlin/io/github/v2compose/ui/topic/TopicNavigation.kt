package io.github.v2compose.ui.topic

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.StringDecoder
import io.github.v2compose.core.composableWithAnimation
import io.github.v2compose.ui.common.OnHtmlImageClick
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPathPart

private const val argsTopicId: String = "topicId"
private const val argsReplyFloor: String = "floor"

const val topicNavigationRoute = "/t/{$argsTopicId}#reply{$argsReplyFloor}"
internal const val topicRefreshRequestKey = "topicRefreshRequest"

data class TopicArgs(val topicId: String, val replyFloor: Int) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) : this(
        stringDecoder.decodeString(checkNotNull(savedStateHandle[argsTopicId])),
        checkNotNull(savedStateHandle[argsReplyFloor]),
    )
}

internal data class TopicNavigationRequest(
    val topicId: String,
    val replyFloor: Int,
)

internal fun topicRoute(
    topicId: String,
    replyFloor: Int = 0,
): String {
    val encodedTopicId = topicId.encodeURLPathPart()
    return "/t/$encodedTopicId#reply$replyFloor"
}

internal fun parseTopicRoute(route: String): TopicNavigationRequest? {
    val routePath = route.substringBefore('?').substringBefore('#')
    val pathSegments = routePath.split('/').filter { it.isNotEmpty() }
    if (pathSegments.size != 2 || pathSegments[0] != "t") return null

    val replyFloor = route.substringAfter('#', missingDelimiterValue = "")
        .removePrefix("reply")
        .toIntOrNull()
        ?: 0
    return TopicNavigationRequest(
        topicId = pathSegments[1].decodeURLPart(),
        replyFloor = replyFloor,
    )
}

internal fun currentTopicId(savedStateHandle: SavedStateHandle?): String? =
    savedStateHandle?.get<String>(argsTopicId)?.decodeURLPart()

internal fun shouldReuseCurrentTopicRoute(
    currentDestinationRoute: String?,
    currentTopicId: String?,
    targetRoute: String,
): Boolean {
    if (currentDestinationRoute != topicNavigationRoute || currentTopicId.isNullOrEmpty()) {
        return false
    }
    return parseTopicRoute(targetRoute)?.topicId == currentTopicId
}

internal fun requestTopicRefresh(savedStateHandle: SavedStateHandle) {
    val nextToken = (savedStateHandle.get<Int>(topicRefreshRequestKey) ?: 0) + 1
    savedStateHandle[topicRefreshRequestKey] = nextToken
}

fun NavController.navigateToTopic(
    topicId: String,
    replyFloor: Int = 0,
    navOptions: NavOptions? = null
) {
    navigate(topicRoute(topicId, replyFloor), navOptions)
}

fun NavGraphBuilder.topicScreen(
    onBackClick: () -> Unit,
    onNodeClick: (String, String) -> Unit,
    onUserAvatarClick: (String, String) -> Unit,
    openUri: (String) -> Unit,
    onAddSupplementClick: (String) -> Unit,
    onHtmlImageClick: OnHtmlImageClick,
    onShareTopic: (String, String) -> Unit,
) {
    composableWithAnimation(
        topicNavigationRoute,
        arguments = listOf(
            navArgument(argsTopicId) { type = NavType.StringType },
            navArgument(argsReplyFloor) { type = NavType.IntType },
        )
    ) { backStackEntry ->
        TopicScreenRoute(
            backStackEntry = backStackEntry,
            onBackClick = onBackClick,
            onNodeClick = onNodeClick,
            onUserAvatarClick = onUserAvatarClick,
            openUri = openUri,
            onAddSupplementClick = onAddSupplementClick,
            onHtmlImageClick = onHtmlImageClick,
            onShareTopic = onShareTopic,
        )
    }
}
