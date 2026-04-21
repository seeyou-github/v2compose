package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SoV2EXSearchResultInfo(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("hits")
    val hits: List<Hit> = emptyList(),
) {
    fun isValid(): Boolean = true

    @Serializable
    data class Hit(
        @SerialName("_source")
        val source: Source = Source(),
        @SerialName("highlight")
        val highlight: Highlight? = null,
    ) {
        @Serializable
        data class Source(
            @SerialName("id")
            val id: Int = 0,
            @SerialName("title")
            val title: String = "",
            @SerialName("content")
            val content: String = "",
            @SerialName("node")
            val node: Int = 0,
            @SerialName("replies")
            val replies: Int = 0,
            @SerialName("created")
            val time: String = "",
            @SerialName("member")
            val creator: String = "",
        )

        @Serializable
        data class Highlight(
            @SerialName("title")
            val title: List<String> = emptyList(),
            @SerialName("content")
            val content: List<String> = emptyList(),
            @SerialName("postscript_list.content")
            val postscriptListContent: List<String> = emptyList(),
            @SerialName("reply_list.content")
            val replyListContent: List<String> = emptyList(),
        )
    }
}
