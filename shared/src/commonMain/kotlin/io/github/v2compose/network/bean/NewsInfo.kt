package io.github.v2compose.network.bean

import io.github.fruit.RawResponseHolder
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class NewsInfo(
    override val rawResponse: String = "",
    @property:Pick("div.box a[href*=mission/daily]")
    val checkInTips: String = "",
    @property:Pick(value = "input.super.special.button", attr = "value")
    val unread: String = "",
    @property:Pick("div.cell.item")
    val items: List<Item> = emptyList(),
    @property:Pick("form[action=/2fa]")
    val twoStepStr: String = "",
    @property:Pick("a.balance_area")
    val balance: String = "",
) : RawResponseHolder {
    fun hasCheckingInTips(): Boolean = checkInTips.isNotEmpty()

    fun unreadCount(): Int {
        return unread.split(" ").getOrNull(0)?.toIntOrNull() ?: 0
    }

    fun balanceGold(): Int = balancePart(0)
    fun balanceSilver(): Int = balancePart(1)
    fun balanceBronze(): Int = balancePart(2)

    fun isValid(): Boolean {
        if (twoStepStr.contains("两步验证")) return false
        return items.isEmpty() || items[0].userName.isNotEmpty()
    }

    private fun balancePart(partIndex: Int): Int {
        if (balance.isEmpty()) return 0
        return try {
            val itemTexts = balance.split(" ")
            val index = itemTexts.size - 3 + partIndex
            if (index >= 0) itemTexts[index].toInt() else 0
        } catch (e: Exception) {
            0
        }
    }

    @Pulp
    data class Item(
        @property:Pick(value = "span.item_title > a")
        val title: String = "",
        @property:Pick(value = "span.item_title > a", attr = "href")
        val linkPath: String = "",
        @property:Pick(value = "td > a > img", attr = "src")
        val avatar: String = "",
        @property:Pick(value = "td > a", attr = "href")
        val avatarLink: String = "",
        @property:Pick(value = "span.small.fade > strong > a")
        val userName: String = "",
        @property:Pick(value = "span.small.fade:last-child", attr = "ownText")
        val timeText: String = "",
        @property:Pick(value = "span.small.fade > a")
        val tagName: String = "",
        @property:Pick(value = "span.small.fade > a", attr = "href")
        val tagLink: String = "",
        @property:Pick("a[class^=count_]")
        val replies: Int = 0,
    ) {
        val id: String
            get() = linkPath.substringAfterLast("/").substringBefore("#").substringBefore("?")

        val time: String
            get() = if (timeText.contains("•")) timeText.split("•")[0].trim() else timeText

        fun tagId(): String? {
            if (tagLink.isEmpty()) return null
            return tagLink.substringAfterLast("/")
        }
    }
}
