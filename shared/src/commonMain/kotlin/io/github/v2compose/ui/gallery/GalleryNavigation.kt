package io.github.v2compose.ui.gallery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument


fun NavGraphBuilder.galleryScreen(onBackClick: () -> Unit) {
    composable(
        galleryNavigationRoute,
        arguments = listOf(
            navArgument(galleryArgsCurrent) { type = NavType.StringType },
            navArgument(galleryArgsPics) { type = NavType.StringType },
        )
    ) {
        GalleryScreenRoute(onBackClick = onBackClick)
    }
}
