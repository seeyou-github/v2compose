package io.github.v2compose.shared.bean

import kotlinx.serialization.Serializable

@Serializable
data class TopicNode(
    val name: String = "",
    val title: String = "",
    val topics: Int = 0,
    val aliases: List<String> = listOf(),
)
