package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class RecentTopics(
    @property:Pick("div.header span.fade")
    val totalText: String = "",
    @property:Pick("div.box div.cell.item")
    val items: List<Item> = emptyList(),
    @property:Pick("div.inner:last-child strong.fade")
    val pageInfo: String = "",
) {
    fun total(): Int = totalText.split(" ").getOrNull(1)?.toIntOrNull() ?: -1

    fun currentPage(): Int = pageInfo.split("/").getOrNull(0)?.toIntOrNull() ?: -1

    fun pageCount(): Int = pageInfo.split("/").getOrNull(1)?.toIntOrNull() ?: -1

    fun isValid(): Boolean = total() >= 0

    @Pulp
    data class Item(
        @property:Pick(value = "span.item_title > a")
        val title: String = "",
        @property:Pick(value = "span.item_title > a", attr = "href")
        val linkPath: String = "",
        @property:Pick(value = "td > a > img", attr = "src")
        val avatarUrl: String = "",
        @property:Pick(value = "span.small.fade > strong > a")
        val userName: String = "",
        @property:Pick(value = "span.small.fade:last-child", attr = "ownText")
        val timeText: String = "",
        @property:Pick(value = "span.small.fade > a")
        val nodeTitle: String = "",
        @property:Pick(value = "span.small.fade > a", attr = "href")
        val nodeLink: String = "",
        @property:Pick("a[class^=count_]")
        val replies: Int = 0,
    ) {
        val id: String
            get() = linkPath.substringAfterLast("/").substringBefore("#").substringBefore("?")

        val avatar: String
            get() = avatarUrl

        val time: String
            get() = if (timeText.contains("•")) timeText.split("•").first() else timeText

        val nodeName: String
            get() = nodeLink.substringAfterLast("/")
    }
}
