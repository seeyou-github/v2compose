package io.github.v2compose.ui.login.twostep

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val twoStepLoginNavigationRoute = "/2fa"

fun NavController.navigateToTwoStepLogin() {
    navigate(twoStepLoginNavigationRoute)
}

fun NavGraphBuilder.twoStepLoginScreen(
    onCloseClick: () -> Unit,
) {
    composable(
        twoStepLoginNavigationRoute,
    ) {
        TwoStepLoginScreenRoute(
            onCloseClick = onCloseClick,
        )
    }
}