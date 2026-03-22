package io.github.v2compose.core

import android.net.Uri

class UriDecoder constructor() : StringDecoder {
    override fun decodeString(encodedString: String): String = Uri.decode(encodedString)
}