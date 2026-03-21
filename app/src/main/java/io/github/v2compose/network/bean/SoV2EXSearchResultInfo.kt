package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
class SoV2EXSearchResultInfo : BaseInfo() {
    @SerializedName("total")
    val total: Int = 0

    @SerializedName("hits")
    val hits: List<Hit> = listOf()

    override fun toString(): String {
        return "SoV2EXSearchResultInfo(total=$total, hits=$hits)"
    }

    override fun isValid(): Boolean {
        return true
    }

    @Stable
    class Hit {
        @SerializedName("_source")
        lateinit var source: Source

        @SerializedName("highlight")
        val highlight: Highlight? = null

        override fun toString(): String {
            return "Hit(source=$source, highlight=$highlight)"
        }

        @Stable
        class Source {
            @SerializedName("id")
            val id: String = ""

            @SerializedName("title")
            val title: String = ""

            @SerializedName("content")
            val content: String = ""

            @SerializedName("node")
            val nodeName: String = ""

            @SerializedName("replies")
            val replies: Int = 0

            @SerializedName("created")
            val time: String = ""

            @SerializedName("member")
            val creator: String = ""

            override fun toString(): String {
                return "Source(id='$id', title='$title', content='$content', nodeName='$nodeName', replies=$replies, time='$time', creator='$creator')"
            }
        }

        @Stable
        class Highlight {
            @SerializedName("title")
            val title: List<String> = listOf()

            @SerializedName("content")
            val content: List<String> = listOf()

            @SerializedName("postscript_list.content")
            val postscriptListContent: List<String> = listOf()

            @SerializedName("reply_list.content")
            val replyListContent: List<String> = listOf()

            override fun toString(): String {
                return "Highlight(title=$title, content=$content, postscriptListContent=$postscriptListContent, replyListContent=$replyListContent)"
            }
        }
    }
}
