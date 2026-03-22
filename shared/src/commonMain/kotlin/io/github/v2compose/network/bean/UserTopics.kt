package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class UserTopics : BaseInfo() {

    @Pick("div.header strong.gray")
    var total: Int = -1

    @Pick("div.box div.cell.item")
    var items: List<Item> = listOf()

    @Pick("div.inner:last-child strong.fade")
    var pageInfo: String = ""

    @Pick("div.cell .topic_content")
    var visibility: String = ""

    fun currentPage(): Int = pageInfo.split("/").getOrNull(0)?.toIntOrNull() ?: -1

    fun pageCount(): Int = pageInfo.split("/").getOrNull(1)?.toIntOrNull() ?: -1

    override fun isValid(): Boolean {
        return total >= 0
    }

    override fun toString(): String {
        return "UserTopics(" +
                "total=$total, " +
                "items=$items, " +
                "pageInfo='$pageInfo', " +
                ")"
    }

    @Pulp
    class Item {

        @Pick(value = "span.item_title a", attr = Attrs.HREF)
        var link: String = ""

        @Pick("strong > a[href^=/member/]:first-child")
        var userName: String = ""

        @Pick("span.item_title")
        var title: String = ""

        @Pick(value = "a.node", attr = Attrs.HREF)
        var nodeLink: String = ""

        @Pick("a.node")
        var nodeTitle: String = ""

        @Pick("span.small.fade:last-child")
        var lastReply: String = ""

        @Pick("a[class^=count_]")
        var repliesNum: Int = 0

        override fun toString(): String {
            return "Item(link='$link', userName='$userName', title='$title', nodeLink='$nodeLink', nodeTitle='$nodeTitle', lastReply='$lastReply', repliesNum=$repliesNum)"
        }

    }

}