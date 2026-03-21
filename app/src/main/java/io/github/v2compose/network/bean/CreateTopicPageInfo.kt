package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 05/06/2017.
 */
@Pulp("div#Wrapper")
class CreateTopicPageInfo : BaseInfo() {
    @Pick(value = "input[name=once]", attr = "value")
    val once: String = ""

    @Pick("div.problem")
    val problem: Problem? = null

    fun toPostMap(title: String, content: String, nodeName: String): Map<String, String> {
        return mapOf(
            "title" to title,
            "content" to content,
            "node_name" to nodeName,
            "once" to once
        )
    }

    override fun isValid(): Boolean {
        return once.isNotEmpty()
    }

    override fun toString(): String {
        return "CreateTopicPageInfo(once='$once', problem=$problem)"
    }

    @Pulp
    class Problem : Serializable {
        @Pick(attr = Attrs.HTML)
        val html: String = ""

        @Pick(attr = Attrs.OWN_TEXT)
        val title: String = ""

        @Pick("ul li")
        val tips: List<String> = listOf()

        fun isEmpty(): Boolean {
            return tips.isEmpty() && title.isEmpty()
        }

        override fun toString(): String {
            return "Problem(html='$html', title='$title', tips=$tips)"
        }
    }
}
