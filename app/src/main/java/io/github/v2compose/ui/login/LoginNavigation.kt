package io.github.v2compose.ui.login

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

private const val argsNext = "next"
const val loginNavigationRoute = "/signin?next={$argsNext}"

fun NavController.navigateToLogin(
    next: String? = null,
    navOptions: NavOptions? = null,
) {
    val encodedNext = Uri.encode(next) ?: ""
    navigate("/signin?next=$encodedNext", navOptions = navOptions)
}

fun NavGraphBuilder.loginScreen(
    onCloseClick: () -> Unit,
    onSignInWithGoogleClick: (String) -> Unit,
) {
    composable(
        route = loginNavigationRoute,
        arguments = listOf(navArgument(argsNext) {
            type = NavType.StringType
            nullable = true
        })
    ) {
        val redirect = it.arguments?.getString(argsNext)
        LoginScreenRoute(
            onCloseClick = onCloseClick,
            onSignInWithGoogleClick = onSignInWithGoogleClick,
            redirect = redirect,
        )
    }
}