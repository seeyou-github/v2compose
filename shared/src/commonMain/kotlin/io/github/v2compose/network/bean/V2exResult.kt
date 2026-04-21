package io.github.v2compose.network.bean

import kotlinx.serialization.Serializable

@Serializable
data class V2exResult(
    val success: Boolean = false,
    val message: String = "",
    val messageEn: String = "",
    val once: Int = -1,
)
