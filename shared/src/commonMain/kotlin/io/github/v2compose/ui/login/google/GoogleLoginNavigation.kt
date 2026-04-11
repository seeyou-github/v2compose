package io.github.v2compose.ui.login.google

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.v2compose.core.composableWithAnimation

private const val argsOnce = "once"

const val googleLoginNavigationRoute = "/auth/google?$argsOnce={$argsOnce}"

fun NavController.navigateToGoogleLogin(once: String) {
    navigate("/auth/google?$argsOnce=$once")
}

fun NavGraphBuilder.googleLoginScreen(onCloseClick: () -> Unit, onLoginSuccess: () -> Unit) {
    composableWithAnimation(
        googleLoginNavigationRoute,
        arguments = listOf(navArgument(argsOnce) { type = NavType.StringType })
    ) {
        val once = it.arguments?.getString(argsOnce) ?: ""
        GoogleLoginScreenRoute(
            once = once,
            onCloseClick = onCloseClick,
            onLoginSuccess = onLoginSuccess,
        )
    }
}
