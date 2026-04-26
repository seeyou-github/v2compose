package io.github.v2compose.core.extension

fun String.fullUrl(baseUrl: String? = null): String {
    if (startsWith("//")) {
        return "https:$this"
    } else if (startsWith("/")) {
        if (baseUrl != null) {
            return baseUrl.dropLastWhile { it == '/' } + this
        }
    }
    return this
}
