package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class UserTopics(
    @property:Pick("div.header strong.gray")
    val total: Int = -1,
    @property:Pick("div.box div.cell.item")
    val items: List<Item> = emptyList(),
    @property:Pick("div.inner:last-child strong.fade")
    val pageInfo: String = "",
    @property:Pick("div.cell .topic_content")
    val visibility: String = "",
) {
    fun currentPage(): Int = pageInfo.split("/").getOrNull(0)?.toIntOrNull() ?: -1

    fun pageCount(): Int = pageInfo.split("/").getOrNull(1)?.toIntOrNull() ?: -1

    fun isValid(): Boolean = total >= 0

    @Pulp
    data class Item(
        @property:Pick(value = "span.item_title a", attr = Attrs.HREF)
        val link: String = "",
        @property:Pick("strong > a[href^=/member/]:first-child")
        val userName: String = "",
        @property:Pick("span.item_title")
        val title: String = "",
        @property:Pick(value = "a.node", attr = Attrs.HREF)
        val nodeLink: String = "",
        @property:Pick("a.node")
        val nodeTitle: String = "",
        @property:Pick("span.small.fade:last-child")
        val lastReply: String = "",
        @property:Pick("a[class^=count_]")
        val repliesNum: Int = 0,
    )
}
