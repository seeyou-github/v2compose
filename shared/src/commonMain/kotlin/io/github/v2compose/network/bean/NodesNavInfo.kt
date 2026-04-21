package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/
 * bottom box
 */
@Pulp("div#Wrapper")
data class NodesNavInfo(
    @property:Pick("div.box:last-child div > table")
    val items: List<Item> = emptyList(),
) {
    fun isValid(): Boolean = items.isEmpty() || items[0].category.isNotEmpty()

    @Pulp
    data class Item(
        @property:Pick("span.fade")
        val category: String = "",
        @property:Pick("a")
        val nodes: List<NodeItem> = emptyList(),
    ) {
        @Pulp
        data class NodeItem(
            @property:Pick
            val title: String = "",
            @property:Pick(attr = Attrs.HREF)
            val link: String = "",
        ) {
            val name: String
                get() = if (link.length > 4) link.substring(4) else ""
        }
    }
}
