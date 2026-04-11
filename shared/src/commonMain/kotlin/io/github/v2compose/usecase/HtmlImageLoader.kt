package io.github.v2compose.usecase

import kotlinx.coroutines.flow.Flow

interface HtmlImageLoader {
    suspend fun loadHtmlImages(html: String, src: String?): Flow<String>
}
