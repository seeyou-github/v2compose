package io.github.v2compose.ui.appearance

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.Json

private val JsonCodec = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

fun parseOverrides(json: String): AppearanceOverrides =
    runCatching {
        if (json.isBlank()) AppearanceOverrides() else JsonCodec.decodeFromString(AppearanceOverrides.serializer(), json)
    }.getOrDefault(AppearanceOverrides())

fun encodeOverrides(overrides: AppearanceOverrides): String =
    JsonCodec.encodeToString(AppearanceOverrides.serializer(), overrides)

fun Color.toArgbInt(): Int = toArgb()

fun colorFromArgbInt(argb: Int): Color = Color(argb)
