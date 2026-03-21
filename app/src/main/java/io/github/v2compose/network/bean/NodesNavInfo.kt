package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 21/05/2017.
 * https://www.v2ex.com/
 * bottom box
 */
@Pulp("div.box:last-child div > table")
class NodesNavInfo : ArrayList<NodesNavInfo.Item>(), IBase {
    private var responseBody: String = ""

    override fun getResponse(): String = responseBody

    override fun setResponse(response: String) {
        responseBody = response
    }

    override fun isValid(): Boolean {
        if (isEmpty()) return true
        return this[0].category.isNotEmpty()
    }

    @Pulp
    class Item : Serializable {
        @Pick("span.fade")
        val category: String = ""

        @Pick("a")
        val nodes: List<NodeItem> = listOf()

        override fun toString(): String {
            return "Item(category='$category', nodes=$nodes)"
        }

        @Pulp
        class NodeItem : Serializable {
            @Pick
            val title: String = ""

            @Pick(attr = Attrs.HREF)
            val link: String = ""

            val name: String
                get() = if (link.length > 4) link.substring(4) else ""

            override fun toString(): String {
                return "NodeItem(title='$title', link='$link')"
            }
        }
    }
}
