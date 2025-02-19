package io.github.v2compose.ui.gallery

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.v2compose.core.StringDecoder

private const val argsCurrent = "current"
private const val argsPics = "pics"
const val galleryNavigationRoute = "/gallery?$argsCurrent={$argsCurrent}&$argsPics={$argsPics}"

data class GalleryScreenArgs(val current: String, val pics: List<String>) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) :
            this(
                stringDecoder.decodeString(checkNotNull(savedStateHandle[argsCurrent])),
                checkNotNull(savedStateHandle.get<String>(argsPics)).split(",").map {
                    stringDecoder.decodeString(it)
                },
            )
}

fun NavController.navigateToGallery(current: String, pics: List<String>) {
    val encodedPics = pics.joinToString(separator = ",") { Uri.encode(it) }
    val route = "/gallery?$argsCurrent=${Uri.encode(current)}&$argsPics=$encodedPics"
    navigate(route)
}


fun NavGraphBuilder.galleryScreen(onBackClick: () -> Unit) {
    composable(
        galleryNavigationRoute,
        arguments = listOf(
            navArgument(argsCurrent) { type = NavType.StringType },
            navArgument(argsPics) { type = NavType.StringType },
        )
    ) {
        GalleryScreenRoute(onBackClick = onBackClick)
    }
}