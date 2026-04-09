package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class RecentTopics : BaseInfo() {

    @Pick("div.header span.fade")
    var totalText: String = ""

    @Pick("div.box div.cell.item")
    var items: List<Item> = listOf()

    @Pick("div.inner:last-child strong.fade")
    var pageInfo: String = ""

    var _total: Int = -1
    fun total(): Int {
        if (_total < 0) {
            _total = totalText.split(" ").getOrNull(1)?.toIntOrNull() ?: -1
        }
        return _total
    }

    var _currentPage: Int = -1
    fun currentPage(): Int {
        if (_currentPage < 0) {
            pageInfo.split("/").getOrNull(0)?.toIntOrNull()?.let { _currentPage = it }
        }
        return _currentPage
    }

    var _pageCount: Int = -1
    fun pageCount(): Int {
        if (_pageCount < 0) {
            pageInfo.split("/").getOrNull(1)?.toIntOrNull()?.let { _pageCount = it }
        }
        return _pageCount
    }

    override fun isValid() = total() >= 0

    override fun toString(): String {
        return "RecentTopics(totalText='$totalText', items=$items, pageInfo='$pageInfo', total=${total()}, currentPage=${currentPage()}, pageCount=${pageCount()})"
    }

    @Pulp
    class Item {

        @Pick(value = "span.item_title > a")
        var title: String = ""

        @Pick(value = "span.item_title > a", attr = "href")
        var linkPath: String = ""

        @Pick(value = "td > a > img", attr = "src")
        var avatarUrl: String = ""

        @Pick(value = "span.small.fade > strong > a")
        var userName: String = ""

        @Pick(value = "span.small.fade:last-child", attr = "ownText")
        var timeText: String = ""

        @Pick(value = "span.small.fade > a")
        var nodeTitle: String = ""

        @Pick(value = "span.small.fade > a", attr = "href")
        var nodeLink: String = ""

        @Pick("a[class^=count_]")
        var replies = 0

        var _id: String = ""
        val id: String
            get() {
                if (_id.isEmpty()) _id =
                    linkPath.substringAfterLast("/").substringBefore("#").substringBefore("?")
                return _id
            }

        val avatar: String
            get() = avatarUrl // TODO: AvatarUtils 迁移后恢复

        var _time: String = ""
        val time: String
            get() {
                if (_time.isEmpty() && timeText.contains("•")) {
                    _time = timeText.split("•").first()
                }
                return _time
            }

        var _nodeName: String = ""
        val nodeName: String
            get() {
                if (_nodeName.isEmpty()) {
                    _nodeName = nodeLink.substring(nodeLink.lastIndexOf("/") + 1)
                }
                return _nodeName
            }

        override fun toString(): String {
            return "Item(title='$title', linkPath='$linkPath', avatarUrl='$avatarUrl', userName='$userName', timeText='$timeText', nodeTitle='$nodeTitle', nodeLink='$nodeLink', replies=$replies, avatar='${avatar}', time='${time}', nodeName='${nodeName}')"
        }
    }
}