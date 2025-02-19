package io.github.v2compose.ui.login.google

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

private const val argsOnce = "once"

const val googleLoginNavigationRoute = "/auth/google?$argsOnce={$argsOnce}"

fun NavController.navigateToGoogleLogin(once: String) {
    navigate("/auth/google?$argsOnce=$once")
}

fun NavGraphBuilder.googleLoginScreen(onCloseClick: () -> Unit, onLoginSuccess: () -> Unit) {
    composable(
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
