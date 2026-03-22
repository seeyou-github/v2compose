package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

@Pulp("div#Wrapper")
class AppendTopicPageInfo : BaseInfo() {
    @Pick(value = "input[name=once]", attr = "value")
    val once: String = ""

    @Pick("div.inner ul li")
    val tips: List<Tip> = listOf()

    @Pick("div.problem")
    val problem: Problem? = null

    override fun isValid(): Boolean {
        return once.isNotEmpty() && tips.size > 1
    }

    override fun toString(): String {
        return "AppendTopicPageInfo(once='$once', tips=$tips, problem=$problem)"
    }

    @Pulp
    class Tip : Serializable {
        @Pick
        val text: String = ""

        override fun toString(): String {
            return "Tip(text='$text')"
        }
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
