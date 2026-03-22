package io.github.v2compose.ui.gallery

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.github.v2compose.core.StringDecoder

private const val TAG = "GalleryViewModel"

class GalleryViewModel constructor(
    savedStateHandle: SavedStateHandle,
    stringDecoder: StringDecoder
) : ViewModel() {

    val screenArgs = GalleryScreenArgs(savedStateHandle, stringDecoder)

    init {
        Log.d(TAG, "args = $screenArgs")
    }

}