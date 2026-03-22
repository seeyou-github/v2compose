package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 18/05/2017.
 * https://www.v2ex.com/my/nodes
 */
@Pulp("div#my-nodes")
class MyNodesInfo : BaseInfo() {
    @Pick("a.fav-node")
    val items: List<Item> = listOf()

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].title.isNotEmpty()
    }

    @Pulp
    class Item : Serializable {
        @Pick(value = "img", attr = Attrs.SRC)
        val avatar: String = ""

        @Pick(value = "span.fav-node-name", attr = Attrs.OWN_TEXT)
        val title: String = ""

        @Pick(value = "span.fade.f12")
        val topicNum: Int = 0

        @Pick(attr = Attrs.HREF)
        val link: String = ""

        private var _name: String = ""

        fun avatarUrl(): String = avatar

        val name: String
            get() {
                if (_name.isNotEmpty()) return _name
                if (link.startsWith("/go/")) {
                    _name = link.substring("/go/".length)
                }
                return _name
            }

        override fun toString(): String {
            return "Item(avatar='$avatar', title='$title', topicNum=$topicNum, link='$link')"
        }
    }
}
