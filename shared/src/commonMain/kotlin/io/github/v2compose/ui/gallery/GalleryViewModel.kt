package io.github.v2compose.ui.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.v2compose.core.StringDecoder

class GalleryViewModel(
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder,
) : ViewModel() {
    val screenArgs = GalleryScreenArgs(savedStateHandle, stringDecoder)
}
