package io.github.v2compose.ui.login.twostep

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation
import io.ktor.http.encodeURLParameter

internal const val twoStepArgsOnce = "once"
const val twoStepLoginNavigationRoute = "/2fa?$twoStepArgsOnce={$twoStepArgsOnce}"

fun NavController.navigateToTwoStepLogin(once: String? = null) {
    val encodedOnce = once?.takeIf { it.isNotBlank() }?.encodeURLParameter()
    navigate(encodedOnce?.let { "/2fa?$twoStepArgsOnce=$it" } ?: "/2fa")
}

fun NavGraphBuilder.twoStepLoginScreen(
    onCloseClick: () -> Unit,
) {
    composableWithAnimation(
        twoStepLoginNavigationRoute,
        arguments = listOf(navArgument(twoStepArgsOnce) {
            type = NavType.StringType
            nullable = true
        }),
    ) {
        TwoStepLoginScreenRoute(
            onCloseClick = onCloseClick,
        )
    }
}
