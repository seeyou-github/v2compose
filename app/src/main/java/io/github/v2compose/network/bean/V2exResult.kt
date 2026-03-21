package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pulp

@Pulp
data class V2exResult(
    val success: Boolean = false,
    val message: String = "",
    val messageEn: String = "",
    val once: Int = -1,
)