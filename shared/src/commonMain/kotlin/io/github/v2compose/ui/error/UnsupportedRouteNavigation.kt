package io.github.v2compose.ui.error

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.savedstate.read
import io.github.v2compose.core.composableWithAnimation
import io.github.v2compose.unsupportedNavigationBaseRoute
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLParameter

private const val argsRoute = "route"
const val unsupportedRouteNavigationRoute = "$unsupportedNavigationBaseRoute?$argsRoute={$argsRoute}"

fun NavController.navigateToUnsupportedRoute(route: String) {
    navigate("$unsupportedNavigationBaseRoute?$argsRoute=${route.encodeURLParameter()}")
}

fun NavGraphBuilder.unsupportedRouteScreen(
    onBackClick: () -> Unit,
    onNavigateHomeClick: () -> Unit,
    onOpenInBrowserClick: (String) -> Unit,
) {
    composableWithAnimation(
        route = unsupportedRouteNavigationRoute,
        arguments = listOf(navArgument(argsRoute) { type = NavType.StringType }),
    ) { backStackEntry ->
        UnsupportedRouteScreen(
            route = backStackEntry.arguments
                ?.read { getStringOrNull(argsRoute) }
                ?.decodeURLPart()
                .orEmpty(),
            onBackClick = onBackClick,
            onNavigateHomeClick = onNavigateHomeClick,
            onOpenInBrowserClick = onOpenInBrowserClick,
        )
    }
}
