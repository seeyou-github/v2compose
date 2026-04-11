package io.github.v2compose.ui.login.twostep

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import io.github.v2compose.core.composableWithAnimation

const val twoStepLoginNavigationRoute = "/2fa"

fun NavController.navigateToTwoStepLogin() {
    navigate(twoStepLoginNavigationRoute)
}

fun NavGraphBuilder.twoStepLoginScreen(
    onCloseClick: () -> Unit,
) {
    composableWithAnimation(
        twoStepLoginNavigationRoute,
    ) {
        TwoStepLoginScreenRoute(
            onCloseClick = onCloseClick,
        )
    }
}