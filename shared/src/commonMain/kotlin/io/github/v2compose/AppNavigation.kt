package io.github.v2compose

import io.github.v2compose.core.extension.fullUrl
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPathPart

const val rootNavigationRoute = "/"
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
    val parsed = parseAppUri(uri) ?: return AppNavigationAction.Ignore
    if (parsed.scheme in systemSchemes) {
        return AppNavigationAction.External(parsed.normalizedAbsoluteUrl)
    }
    if (parsed.host != null && !parsed.host.endsWith(Constants.host)) {
        return AppNavigationAction.External(parsed.normalizedAbsoluteUrl)
    }

    val screenType = parsed.pathSegments.getOrNull(0).orEmpty()
    val screenId = parsed.pathSegments.getOrNull(1).orEmpty()
    return when (screenType) {
        "t" -> {
            val replyFloor = parsed.fragment.removePrefix("reply").toIntOrNull() ?: 0
            AppNavigationAction.Navigate(AppRoutes.topic(screenId, replyFloor))
        }

        "go" -> AppNavigationAction.Navigate(AppRoutes.node(screenId))
        "member" -> AppNavigationAction.Navigate(AppRoutes.user(screenId))
        else -> AppNavigationAction.Navigate(AppRoutes.webView(parsed.normalizedAbsoluteUrl))
    }
}

internal fun resolveRedirectLocation(location: String): AppNavigationAction {
    val parsed = parseAppUri(location) ?: return AppNavigationAction.Ignore
    if (parsed.host != null && !parsed.host.endsWith(Constants.host)) {
        return AppNavigationAction.External(parsed.normalizedAbsoluteUrl)
    }

    val screenType = parsed.pathSegments.getOrNull(0).orEmpty()
    return when (screenType) {
        "" -> AppNavigationAction.Navigate(
            route = parsed.route,
            clearBackStackToRoot = screenType.isEmpty(),
        )

        "signin" -> AppNavigationAction.Navigate(normalizeRoute(parsed.route, "/signin"))
        "2fa" -> AppNavigationAction.Navigate(normalizeRoute(parsed.route, "/2fa"))
        else -> AppNavigationAction.Navigate(AppRoutes.unsupported(parsed.route))
    }
}

private val systemSchemes = setOf("mailto", "sms", "tel")

private data class ParsedAppUri(
    val normalizedAbsoluteUrl: String,
    val route: String,
    val host: String?,
    val scheme: String?,
    val pathSegments: List<String>,
    val fragment: String,
)

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
