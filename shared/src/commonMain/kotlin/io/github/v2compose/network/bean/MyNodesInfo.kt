package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/my/nodes
 */
@Pulp("div#my-nodes")
data class MyNodesInfo(
    @property:Pick("a.fav-node")
    val items: List<Item> = emptyList(),
) {
    fun isValid(): Boolean = items.isEmpty() || items[0].title.isNotEmpty()

    @Pulp
    data class Item(
        @property:Pick(value = "img", attr = Attrs.SRC)
        val avatar: String = "",
        @property:Pick(value = "span.fav-node-name", attr = Attrs.OWN_TEXT)
        val title: String = "",
        @property:Pick(value = "span.fade.f12")
        val topicNum: Int = 0,
        @property:Pick(attr = Attrs.HREF)
        val link: String = "",
    ) {
        fun avatarUrl(): String = avatar

        val name: String
            get() = if (link.startsWith("/go/")) link.substring("/go/".length) else ""
    }
}
