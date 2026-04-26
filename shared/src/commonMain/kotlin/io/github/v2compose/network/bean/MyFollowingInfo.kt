package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

/**
 * https://www.v2ex.com/my/following?p=1
 */
@Slice("div#Wrapper")
data class MyFollowingInfo(
    @property:Pick(value = "input.page_input", attr = "max")
    val totalPageCount: Int = 0,
    @property:Pick("div.cell.item")
    val items: List<Item> = emptyList(),
) {
    fun isValid(): Boolean = items.isEmpty() || items[0].userName.isNotEmpty()

    @Slice
    data class Item(
        @property:Pick(value = "img.avatar", attr = Attrs.SRC)
        val avatar: String = "",
        @property:Pick("strong a[href^=/member/]")
        val userName: String = "",
        @property:Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        val time: String = "",
        @property:Pick("span.item_title a[href^=/t/]")
        val title: String = "",
        @property:Pick(value = "span.item_title a[href^=/t/]", attr = Attrs.HREF)
        val link: String = "",
        @property:Pick("a[class^=count_]")
        val commentNum: Int = 0,
        @property:Pick("a.node")
        val tagTitle: String = "",
        @property:Pick(value = "a.node", attr = Attrs.HREF)
        val tagLink: String = "",
    ) {
        fun getId(): String {
            if (!link.startsWith("/t/")) return ""
            val end = link.indexOf('#')
            return if (end > 0) link.substring("/t/".length, end) else link.substring("/t/".length)
        }

        fun getAdjustedAvatar(): String = avatar

        fun getTagName(): String {
            return if (tagLink.startsWith("/go/")) tagLink.substring("/go/".length) else ""
        }
    }
}
