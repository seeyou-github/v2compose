package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Node(
    val id: Int = 0,
    val name: String = "",
    val title: String = "",
    val url: String = "",
    val topics: Int = 0,
    val stars: Int = 0,
    @SerialName("avatar_large")
    val avatarLarge: String = "",
    @SerialName("avatar_normal")
    val avatarNormal: String = "",
    @SerialName("avatar_mini")
    val avatarMini: String = "",
    @SerialName("title_alternative")
    val titleAlternative: String = "",
    @SerialName("header")
    val header: String = "",
    @SerialName("footer")
    val footer: String = "",
    val root: Boolean = false,
    @SerialName("parent_node_name")
    val parentNodeName: String = "",
    val aliases: List<String> = listOf(),
) {
    val avatar: String
        get() = avatarLarge.ifEmpty { avatarNormal.ifEmpty { avatarMini } }
}
