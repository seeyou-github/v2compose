package io.github.v2compose

import io.github.v2compose.core.extension.fullUrl
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPathPart

const val rootNavigationRoute = "/"
internal const val authSigninRoute = "/signin"
internal const val authTwoStepRoute = "/2fa"
const val unsupportedNavigationBaseRoute = "/unsupported"

internal sealed interface AppNavigationAction {
    data class Navigate(
        val route: String,
        val clearBackStackToRoot: Boolean = false,
    ) : AppNavigationAction

    data class External(val uri: String) : AppNavigationAction

    data object Ignore : AppNavigationAction
}

internal object AppRoutes {
    fun topic(topicId: String, replyFloor: Int = 0): String =
        "/t/${topicId.encodeURLPathPart()}#reply$replyFloor"

    fun node(nodeName: String, nodeTitle: String? = null): String {
        val encodedNodeName = nodeName.encodeURLPathPart()
        val encodedNodeTitle = nodeTitle?.encodeURLParameter().orEmpty()
        return "/go/$encodedNodeName?nodeTitle=$encodedNodeTitle"
    }

    fun user(userName: String, userAvatar: String? = null): String {
        val encodedUserName = userName.encodeURLPathPart()
        val encodedUserAvatar = userAvatar?.encodeURLParameter().orEmpty()
        return "/member/$encodedUserName?userAvatar=$encodedUserAvatar"
    }

    fun webView(url: String): String = "/webview?url=${url.encodeURLParameter()}"

    fun unsupported(route: String): String =
        "$unsupportedNavigationBaseRoute?route=${route.encodeURLParameter()}"
}

internal fun resolveOpenUri(uri: String): AppNavigationAction {
    return when (val target = resolveNavigationTarget(uri) ?: return AppNavigationAction.Ignore) {
        is AppNavigationTarget.External -> AppNavigationAction.External(target.uri)
        is AppNavigationTarget.Topic ->
            AppNavigationAction.Navigate(AppRoutes.topic(target.topicId, target.replyFloor))

        is AppNavigationTarget.Node ->
            AppNavigationAction.Navigate(AppRoutes.node(target.nodeName))

        is AppNavigationTarget.User ->
            AppNavigationAction.Navigate(AppRoutes.user(target.userName))

        is AppNavigationTarget.Root,
        is AppNavigationTarget.Auth,
        is AppNavigationTarget.UnknownInternal -> AppNavigationAction.Navigate(
            AppRoutes.webView(target.normalizedAbsoluteUrlForWebView()),
        )
    }
}

internal fun resolveRedirectLocation(location: String): AppNavigationAction {
    return when (val target = resolveNavigationTarget(location) ?: return AppNavigationAction.Ignore) {
        is AppNavigationTarget.External -> AppNavigationAction.External(target.uri)
        is AppNavigationTarget.Root -> AppNavigationAction.Navigate(
            route = target.route,
            clearBackStackToRoot = true,
        )

        is AppNavigationTarget.Auth ->
            AppNavigationAction.Navigate(target.normalizedRoute)

        is AppNavigationTarget.Topic ->
            AppNavigationAction.Navigate(AppRoutes.topic(target.topicId, target.replyFloor))

        is AppNavigationTarget.Node ->
            AppNavigationAction.Navigate(AppRoutes.node(target.nodeName))

        is AppNavigationTarget.User ->
            AppNavigationAction.Navigate(AppRoutes.user(target.userName))

        is AppNavigationTarget.UnknownInternal ->
            AppNavigationAction.Navigate(AppRoutes.unsupported(target.route))
    }
}

internal fun authFlowRouteKey(raw: String): String? {
    val parsed = parseAppUri(raw) ?: return null
    return when (parsed.pathSegments.getOrNull(0).orEmpty()) {
        "signin" -> authSigninRoute
        "2fa" -> authTwoStepRoute
        else -> null
    }
}

internal fun isSameAuthFlow(first: String, second: String): Boolean {
    val firstKey = authFlowRouteKey(first) ?: return false
    return firstKey == authFlowRouteKey(second)
}

internal fun shouldIgnoreRepeatedAuthNavigation(currentRoute: String?, targetRoute: String): Boolean {
    val currentKey = currentRoute?.let(::authFlowRouteKey) ?: return false
    return currentKey == authFlowRouteKey(targetRoute)
}

private val systemSchemes = setOf("mailto", "sms", "tel")

private sealed interface AppNavigationTarget {
    data class External(val uri: String) : AppNavigationTarget

    data class Root(
        val route: String,
        val normalizedAbsoluteUrl: String,
    ) : AppNavigationTarget

    data class Auth(
        val normalizedRoute: String,
        val normalizedAbsoluteUrl: String,
    ) : AppNavigationTarget

    data class Topic(
        val topicId: String,
        val replyFloor: Int,
    ) : AppNavigationTarget

    data class Node(val nodeName: String) : AppNavigationTarget

    data class User(val userName: String) : AppNavigationTarget

    data class UnknownInternal(
        val route: String,
        val normalizedAbsoluteUrl: String,
    ) : AppNavigationTarget
}

private data class ParsedAppUri(
    val normalizedAbsoluteUrl: String,
    val route: String,
    val host: String?,
    val scheme: String?,
    val pathSegments: List<String>,
    val fragment: String,
)

private fun resolveNavigationTarget(raw: String): AppNavigationTarget? {
    val parsed = parseAppUri(raw) ?: return null
    return when {
        parsed.scheme in systemSchemes -> AppNavigationTarget.External(parsed.normalizedAbsoluteUrl)
        parsed.host != null && !parsed.host.endsWith(Constants.host) ->
            AppNavigationTarget.External(parsed.normalizedAbsoluteUrl)

        else -> parsed.toInternalNavigationTarget()
    }
}

private fun ParsedAppUri.toInternalNavigationTarget(): AppNavigationTarget {
    val screenType = pathSegments.getOrNull(0).orEmpty()
    val screenId = pathSegments.getOrNull(1).orEmpty()
    return when (screenType) {
        "" -> AppNavigationTarget.Root(
            route = route,
            normalizedAbsoluteUrl = normalizedAbsoluteUrl,
        )

        "signin" -> AppNavigationTarget.Auth(
            normalizedRoute = normalizeRoute(route, authSigninRoute),
            normalizedAbsoluteUrl = normalizedAbsoluteUrl,
        )

        "2fa" -> AppNavigationTarget.Auth(
            normalizedRoute = normalizeRoute(route, authTwoStepRoute),
            normalizedAbsoluteUrl = normalizedAbsoluteUrl,
        )

        "t" -> screenId.takeIf { it.isNotEmpty() }?.let {
            AppNavigationTarget.Topic(
                topicId = it,
                replyFloor = fragment.removePrefix("reply").toIntOrNull() ?: 0,
            )
        } ?: unknownInternalTarget()

        "go" -> screenId.takeIf { it.isNotEmpty() }?.let {
            AppNavigationTarget.Node(it)
        } ?: unknownInternalTarget()

        "member" -> screenId.takeIf { it.isNotEmpty() }?.let {
            AppNavigationTarget.User(it)
        } ?: unknownInternalTarget()

        else -> unknownInternalTarget()
    }
}

private fun ParsedAppUri.unknownInternalTarget(): AppNavigationTarget.UnknownInternal =
    AppNavigationTarget.UnknownInternal(
        route = route,
        normalizedAbsoluteUrl = normalizedAbsoluteUrl,
    )

private fun AppNavigationTarget.normalizedAbsoluteUrlForWebView(): String = when (this) {
    is AppNavigationTarget.Root -> normalizedAbsoluteUrl
    is AppNavigationTarget.Auth -> normalizedAbsoluteUrl
    is AppNavigationTarget.UnknownInternal -> normalizedAbsoluteUrl
    else -> error("Unsupported webview navigation target: $this")
}

private fun parseAppUri(raw: String): ParsedAppUri? {
    val value = raw.trim()
    if (value.isEmpty()) return null

    val scheme = value.substringBefore(':', missingDelimiterValue = "")
        .takeIf { it.isNotEmpty() && !it.contains('/') && !it.contains('?') && !it.contains('#') }

    val (normalizedAbsoluteUrl, route, host) = when {
        scheme in systemSchemes -> Triple(value, value, null)

        value.startsWith("//") -> {
            val absolute = "https:$value"
            val afterScheme = absolute.substringAfter("://")
            val parsedHost = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
                .substringBefore(':')
            val parsedRoute = afterScheme.removePrefix(afterScheme.substringBefore('/').substringBefore('?').substringBefore('#'))
                .ifEmpty { "/" }
                .ensureLeadingSlash()
            Triple(absolute, parsedRoute, parsedHost)
        }

        scheme != null && value.startsWith("$scheme://") -> {
            val afterScheme = value.substringAfter("://")
            val parsedHost = afterScheme.substringBefore('/').substringBefore('?').substringBefore('#')
                .substringBefore(':')
            val parsedRoute = afterScheme.removePrefix(afterScheme.substringBefore('/').substringBefore('?').substringBefore('#'))
                .ifEmpty { "/" }
                .ensureLeadingSlash()
            Triple(value, parsedRoute, parsedHost)
        }

        else -> {
            val parsedRoute = value.ensureLeadingSlash()
            Triple(parsedRoute.fullUrl(Constants.baseUrl), parsedRoute, null)
        }
    }

    val routePath = route.substringBefore('?').substringBefore('#')
    val fragment = route.substringAfter('#', missingDelimiterValue = "")
    val pathSegments = routePath.split('/').filter { it.isNotEmpty() }
    return ParsedAppUri(
        normalizedAbsoluteUrl = normalizedAbsoluteUrl,
        route = route,
        host = host,
        scheme = scheme,
        pathSegments = pathSegments,
        fragment = fragment,
    )
}

private fun String.ensureLeadingSlash(): String = if (startsWith("/")) this else "/$this"

private fun normalizeRoute(route: String, normalizedPath: String): String {
    val query = route.substringAfter('?', missingDelimiterValue = "")
        .substringBefore('#')
        .takeIf { it.isNotEmpty() }
        ?.let { "?$it" }
        .orEmpty()
    val fragment = route.substringAfter('#', missingDelimiterValue = "")
        .takeIf { it.isNotEmpty() }
        ?.let { "#$it" }
        .orEmpty()
    return normalizedPath + query + fragment
}
