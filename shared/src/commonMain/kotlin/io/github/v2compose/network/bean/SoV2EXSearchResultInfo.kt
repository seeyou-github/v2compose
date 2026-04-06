package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SoV2EXSearchResultInfo : BaseInfo() {
    @SerialName("total")
    val total: Int = 0

    @SerialName("hits")
    val hits: List<Hit> = listOf()

    override fun toString(): String {
        return "SoV2EXSearchResultInfo(total=$total, hits=$hits)"
    }

    override fun isValid(): Boolean {
        return true
    }

    @Serializable
    class Hit {
        @SerialName("_source")
        lateinit var source: Source

        @SerialName("highlight")
        val highlight: Highlight? = null

        override fun toString(): String {
            return "Hit(source=$source, highlight=$highlight)"
        }

        @Serializable
        class Source {
            @SerialName("id")
            val id: Int = 0

            @SerialName("title")
            val title: String = ""

            @SerialName("content")
            val content: String = ""

            @SerialName("node")
            val node: Int = 0

            @SerialName("replies")
            val replies: Int = 0

            @SerialName("created")
            val time: String = ""

            @SerialName("member")
            val creator: String = ""

            override fun toString(): String {
                return "Source(id='$id', title='$title', content='$content', node='$node', replies=$replies, time='$time', creator='$creator')"
            }
        }

        @Serializable
        class Highlight {
            @SerialName("title")
            val title: List<String> = listOf()

            @SerialName("content")
            val content: List<String> = listOf()

            @SerialName("postscript_list.content")
            val postscriptListContent: List<String> = listOf()

            @SerialName("reply_list.content")
            val replyListContent: List<String> = listOf()

            override fun toString(): String {
                return "Highlight(title=$title, content=$content, postscriptListContent=$postscriptListContent, replyListContent=$replyListContent)"
            }
        }
    }
}
