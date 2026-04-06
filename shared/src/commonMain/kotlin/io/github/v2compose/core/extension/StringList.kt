package io.github.v2compose.core.extension

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun List<String>.toJson(): String {
    return Json.encodeToString(this)
}

fun String.toStringList(): List<String>? {
    return runCatching { Json.decodeFromString<List<String>>(this) }.getOrNull()
}