package io.github.v2compose.core.extension

import io.github.v2compose.authFlowRouteKey
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders

fun Exception.isRedirect(location: String): Boolean {
    return this is ResponseException && response.status.value in 300..399
            && response.headers[HttpHeaders.Location] == location
}

val Exception.isRedirect: Boolean
    get() {
        return this is ResponseException && response.status.value in 300..399
    }

val Exception.redirectLocation: String?
    get() {
        return if (this is ResponseException && response.status.value in 300..399) {
            response.headers[HttpHeaders.Location]
        } else null
    }

fun Exception.isRedirectToSameAuthFlow(requestRoute: String): Boolean {
    val requestAuthFlow = authFlowRouteKey(requestRoute) ?: return false
    return requestAuthFlow == redirectLocation?.let(::authFlowRouteKey)
}
