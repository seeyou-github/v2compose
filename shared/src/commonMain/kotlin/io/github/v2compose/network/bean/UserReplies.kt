package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class UserReplies(
    @property:Pick("div.header strong.gray")
    val total: Int = -1,
    @property:Pick("div.box:last-child > div.dock_area")
    val dockItems: List<ReplyDockItem> = emptyList(),
    @property:Pick("div.box:last-child div.reply_content")
    val replyContentItems: List<ReplyContentItem> = emptyList(),
    @property:Pick("div.inner:last-child strong.fade")
    val pageInfo: String = "",
) {
    fun items(): List<Item> = dockItems.zip(replyContentItems) { dock, content -> Item(dock, content) }

    fun currentPage(): Int = pageInfo.split("/").getOrNull(0)?.toIntOrNull() ?: -1

    fun pageCount(): Int = pageInfo.split("/").getOrNull(1)?.toIntOrNull() ?: -1

    fun isValid(): Boolean = total >= 0

    @Pulp
    data class ReplyDockItem(
        @property:Pick("span.gray")
        val title: String = "",
        @property:Pick(value = "span.gray > a", attr = Attrs.HREF)
        val link: String = "",
        @property:Pick("span.fade")
        val time: String = "",
    )

    @Pulp
    data class ReplyContentItem(
        @property:Pick(attr = Attrs.HTML)
        val content: String = "",
    )

    data class Item(
        val dock: ReplyDockItem,
        val content: ReplyContentItem
    )
}
