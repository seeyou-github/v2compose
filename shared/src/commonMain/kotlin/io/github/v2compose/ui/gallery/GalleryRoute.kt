package io.github.v2compose.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import io.github.v2compose.core.StringDecoder
import io.ktor.http.encodeURLParameter

const val galleryArgsCurrent = "current"
const val galleryArgsPics = "pics"

const val galleryNavigationRoute =
    "/gallery?$galleryArgsCurrent={$galleryArgsCurrent}&$galleryArgsPics={$galleryArgsPics}"

data class GalleryScreenArgs(val current: String, val pics: List<String>) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) : this(
        current = stringDecoder.decodeString(checkNotNull(savedStateHandle[galleryArgsCurrent])),
        pics = checkNotNull(savedStateHandle.get<String>(galleryArgsPics))
            .split(",")
            .map(stringDecoder::decodeString),
    )
}

fun NavController.navigateToGallery(current: String, pics: List<String>) {
    val encodedPics = pics.joinToString(separator = ",") { it.encodeURLParameter() }
    val route =
        "/gallery?$galleryArgsCurrent=${current.encodeURLParameter()}&$galleryArgsPics=$encodedPics"
    navigate(route)
}
