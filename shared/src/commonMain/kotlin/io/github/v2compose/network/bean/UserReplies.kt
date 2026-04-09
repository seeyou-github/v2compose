package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class UserReplies : BaseInfo() {

    @Pick("div.header strong.gray")
    var total: Int = -1

    @Pick("div.box:last-child > div.dock_area")
    var dockItems: List<ReplyDockItem> = listOf()

    @Pick("div.box:last-child div.reply_content")
    var replyContentItems: List<ReplyContentItem> = listOf()

    fun items(): List<Item> =
        dockItems.zip(replyContentItems) { dock, content -> Item(dock, content) }

    @Pick("div.inner:last-child strong.fade")
    var pageInfo: String = ""

    fun currentPage(): Int = pageInfo.split("/").getOrNull(0)?.toIntOrNull() ?: -1

    fun pageCount(): Int = pageInfo.split("/").getOrNull(1)?.toIntOrNull() ?: -1

    override fun isValid(): Boolean {
        return total >= 0
    }

    override fun toString(): String {
        return "UserReplies(" +
                "total=$total, " +
                "dockItems=$dockItems, " +
                "replyContentItems=$replyContentItems, " +
                "pageInfo='$pageInfo', " +
                ")"
    }

    @Pulp
    class ReplyDockItem {
        @Pick("span.gray")
        var title: String = ""

        @Pick(value = "span.gray > a", attr = Attrs.HREF)
        var link: String = ""

        @Pick("span.fade")
        var time: String = ""

        override fun toString(): String {
            return "ReplyDockItem{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    ", time='" + time + '\'' +
                    '}'
        }
    }

    @Pulp
    class ReplyContentItem {
        @Pick(attr = Attrs.HTML)
        var content: String = ""

        override fun toString(): String {
            return "ReplyContentItem{" +
                    "content='" + content + '\'' +
                    '}'
        }
    }

    data class Item(var dock: ReplyDockItem, var content: ReplyContentItem)
}