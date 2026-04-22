package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

/**
 * https://www.v2ex.com/my/topics
 */
@Slice("div#Wrapper")
data class MyTopicsInfo(
    @property:Pick(value = "input.page_input", attr = "max")
    val totalPageCountText: String = "",
    @property:Pick("div.cell.item")
    val items: List<Item> = emptyList(),
) {
    fun totalPageCount(): Int = totalPageCountText.toIntOrNull() ?: 0

    fun isValid(): Boolean = items.isEmpty() || items[0].title.isNotEmpty()

    @Slice
    data class Item(
        @property:Pick(value = "td>a[href^=/member]", attr = Attrs.HREF)
        val userLink: String = "",
        @property:Pick(value = "img.avatar", attr = Attrs.SRC)
        val avatar: String = "",
        @property:Pick("span.item_title")
        val title: String = "",
        @property:Pick(value = "span.item_title a", attr = Attrs.HREF)
        val link: String = "",
        @property:Pick("a[class^=count_]")
        val commentNum: Int = 0,
        @property:Pick("a.node")
        val tagTitle: String = "",
        @property:Pick(value = "a.node", attr = Attrs.HREF)
        val tagLink: String = "",
        @property:Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        val time: String = "",
    ) {
        val id: String
            get() {
                if (!link.startsWith("/t/")) return ""
                val end = link.indexOf('#')
                return if (end > 0) link.substring("/t/".length, end) else link.substring("/t/".length)
            }

        val userName: String
            get() = userLink.substringAfterLast("/")

        val adjustedAvatar: String
            get() = avatar

        val tagName: String
            get() = if (tagLink.startsWith("/go/")) tagLink.substring("/go/".length) else ""
    }
}
