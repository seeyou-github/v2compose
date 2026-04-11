package io.github.v2compose.core

import io.ktor.http.decodeURLPart

class UrlStringDecoder : StringDecoder {
    override fun decodeString(encodedString: String): String = encodedString.decodeURLPart()
}
