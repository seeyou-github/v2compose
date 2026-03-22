package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class NewsInfo : BaseInfo() {
    @Pick("div.box a[href*=mission/daily]")
    var checkInTips: String = ""

    @Pick(value = "input.super.special.button", attr = "value")
    var unread: String = ""

    @Pick("div.cell.item")
    var items: List<Item> = listOf()

    @Pick("form[action=/2fa]")
    var twoStepStr: String = ""

    @Pick("a.balance_area")
    var balance: String = ""

    fun hasCheckingInTips(): Boolean = checkInTips.isNotEmpty()

    private fun isTwoStepError(): Boolean =
        twoStepStr.isNotEmpty() && twoStepStr.contains("两步验证")

    fun unreadCount(): Int {
        if (unread.isEmpty()) return 0
        return try {
            unread.split(" ").getOrNull(0)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun balanceGold(): Int = getBalancePart(0)
    fun balanceSilver(): Int = getBalancePart(1)
    fun balanceBronze(): Int = getBalancePart(2)

    private fun getBalancePart(partIndex: Int): Int {
        if (balance.isEmpty()) return 0
        return try {
            var itemTexts = balance.split(" ")
            var index = itemTexts.size - 3 + partIndex
            if (index >= 0) {
                itemTexts[index].toInt()
            } else 0
        } catch (e: Exception) {
            0
        }
    }

    override fun toString(): String {
        return "NewsInfo(items=$items)"
    }

    override fun isValid(): Boolean {
        if (isTwoStepError()) return false
        return items.isEmpty() || items[0].userName.isNotEmpty()
    }

    @Pulp
    class Item {
        @Pick(value = "span.item_title > a")
        var title: String = ""

        @Pick(value = "span.item_title > a", attr = "href")
        var linkPath: String = ""

        @Pick(value = "td > a > img", attr = "src")
        var avatar: String = ""

        @Pick(value = "td > a", attr = "href")
        var avatarLink: String = ""

        @Pick(value = "span.small.fade > strong > a")
        var userName: String = ""

        @Pick(value = "span.small.fade:last-child", attr = "ownText")
        var timeText: String = ""

        @Pick(value = "span.small.fade > a")
        var tagName: String = ""

        @Pick(value = "span.small.fade > a", attr = "href")
        var tagLink: String = ""

        @Pick("a[class^=count_]")
        var replies: Int = 0

        var _id: String = ""
        val id: String
            get() {
                if (_id.isBlank()) {
                    _id = linkPath.substringAfterLast("/").substringBefore("#").substringBefore("?")
                }
                return _id
            }

        val time: String
            get() {
                if (timeText.isNotEmpty() && timeText.contains("•")) {
                    return timeText.split("•")[0].trim()
                }
                return timeText
            }

        fun tagId(): String? {
            if (tagLink.isEmpty()) return null
            return tagLink.substring(tagLink.lastIndexOf("/") + 1)
        }

        override fun toString(): String {
            return "Item(title='$title', linkPath='$linkPath', avatar='$avatar', avatarLink='$avatarLink', userName='$userName', timeText='$timeText', tagName='$tagName', tagLink='$tagLink', replies=$replies)"
        }
    }
}
