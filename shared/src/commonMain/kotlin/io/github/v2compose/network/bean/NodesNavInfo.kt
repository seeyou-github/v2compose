package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/
 * bottom box
 */
@Pulp("div.box:last-child div > table")
class NodesNavInfo : MutableList<NodesNavInfo.Item> by mutableListOf(), IBase {
    var responseBody: String = ""

    override fun getResponse(): String = responseBody

    override fun setResponse(html: String) {
        responseBody = html
    }

    override fun isValid(): Boolean {
        if (isEmpty()) return true
        return this[0].category.isNotEmpty()
    }

    @Pulp
    class Item {
        @Pick("span.fade")
        var category: String = ""

        @Pick("a")
        var nodes: List<NodeItem> = listOf()

        override fun toString(): String {
            return "Item(category='$category', nodes=$nodes)"
        }

        @Pulp
        class NodeItem {
            @Pick
            var title: String = ""

            @Pick(attr = Attrs.HREF)
            var link: String = ""

            val name: String
                get() = if (link.length > 4) link.substring(4) else ""

            override fun toString(): String {
                return "NodeItem(title='$title', link='$link')"
            }
        }
    }
}
