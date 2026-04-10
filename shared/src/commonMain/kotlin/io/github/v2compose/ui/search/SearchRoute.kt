package io.github.v2compose.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import io.github.v2compose.core.StringDecoder
import io.ktor.http.encodeURLParameter

const val searchArgsKeyword = "keyword"

const val searchScreenNavigationRoute = "/search?keyword={$searchArgsKeyword}"

data class SearchArgs(val keyword: String?) {
    constructor(savedStateHandle: SavedStateHandle, stringDecoder: StringDecoder) : this(
        keyword = savedStateHandle.get<String>(searchArgsKeyword)?.let(stringDecoder::decodeString)
    )
}

fun NavController.navigateToSearch(keyword: String? = null) {
    val encodedKeyword = keyword?.encodeURLParameter().orEmpty()
    navigate("/search?keyword=$encodedKeyword")
}
