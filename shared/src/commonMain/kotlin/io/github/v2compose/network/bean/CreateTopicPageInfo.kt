package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class CreateTopicPageInfo : BaseInfo() {
    @Pick(value = "input[name=once]", attr = "value")
    var once: String = ""

    @Pick("div.problem")
    var problem: Problem? = null

    override fun isValid(): Boolean {
        return once.isNotEmpty()
    }

    override fun toString(): String {
        return "CreateTopicPageInfo(once='$once', problem=$problem)"
    }

    @Pulp
    class Problem {
        @Pick(attr = Attrs.HTML)
        var html: String = ""

        @Pick(attr = Attrs.OWN_TEXT)
        var title: String = ""

        @Pick("ul li")
        var tips: List<String> = listOf()

        fun isEmpty(): Boolean {
            return tips.isEmpty() && title.isEmpty()
        }

        override fun toString(): String {
            return "Problem(html='$html', title='$title', tips=$tips)"
        }
    }
}
