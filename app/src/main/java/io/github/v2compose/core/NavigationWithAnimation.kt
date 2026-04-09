package io.github.v2compose.core

import io.github.v2compose.R
import androidx.annotation.AnimRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

fun NavController.navigateWithAnimation(
    route: String,
    @AnimRes enterAnim: Int = R.anim.slide_in_right,
    @AnimRes exitAnim: Int = R.anim.slide_out_left,
    @AnimRes popEnterAnim: Int = android.R.anim.slide_in_left,
    @AnimRes popExitAnim: Int = android.R.anim.slide_out_right,
) {
    navigate(
        route,
        NavOptions.Builder().apply {
            setEnterAnim(enterAnim)
            setExitAnim(exitAnim)
            setPopEnterAnim(popEnterAnim)
            setPopExitAnim(popExitAnim)
        }.build(),
    )
}

fun NavGraphBuilder.composableWithAnimation(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = { slideInHorizontally { it } },
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = { slideOutHorizontally { -it } },
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = { slideInHorizontally { -it } },
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = { slideOutHorizontally { it } },
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}