package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import io.github.v2compose.util.AvatarUtils
import io.github.v2compose.util.UriUtils
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 04/04/2017.
 */
@Stable
@Pulp("div#Wrapper")
class NewsInfo : BaseInfo() {
    @Pick("div.box a[href*=mission/daily]")
    private val checkInTips: String = ""

    @Pick(value = "input.super.special.button", attr = "value")
    private val unread: String = ""

    @Pick("div.cell.item")
    val items: List<Item> = listOf()

    @Pick("form[action=/2fa]")
    private val twoStepStr: String = ""

    @Pick("a.balance_area")
    private val balance: String = ""

    fun hasCheckingInTips(): Boolean = checkInTips.isNotEmpty()

    private val isTwoStepError: Boolean
        get() = twoStepStr.isNotEmpty() && twoStepStr.contains("两步验证")

    val unreadCount: Int
        get() {
            if (unread.isEmpty()) return 0
            return try {
                unread.split(" ").getOrNull(0)?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }
        }

    val balanceGold: Int get() = getBalancePart(0)
    val balanceSilver: Int get() = getBalancePart(1)
    val balanceBronze: Int get() = getBalancePart(2)

    private fun getBalancePart(partIndex: Int): Int {
        if (balance.isEmpty()) return 0
        return try {
            val itemTexts = balance.split(" ")
            val index = itemTexts.size - 3 + partIndex
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
        if (isTwoStepError) return false
        return items.isEmpty() || items[0].userName.isNotEmpty()
    }

    @Stable
    @Pulp
    class Item : Serializable {
        @Pick(value = "span.item_title > a")
        val title: String = ""

        @Pick(value = "span.item_title > a", attr = "href")
        val linkPath: String = ""

        @Pick(value = "td > a > img", attr = "src")
        val avatar: String = ""

        @Pick(value = "td > a", attr = "href")
        val avatarLink: String = ""

        @Pick(value = "span.small.fade > strong > a")
        val userName: String = ""

        @Pick(value = "span.small.fade:last-child", attr = "ownText")
        val timeText: String = ""

        @Pick(value = "span.small.fade > a")
        val tagName: String = ""

        @Pick(value = "span.small.fade > a", attr = "href")
        private val tagLink: String = ""

        @Pick("a[class^=count_]")
        val replies: Int = 0

        val id: String
            get() = UriUtils.getLastSegment(linkPath)

        val adjustedAvatar: String
            get() = AvatarUtils.adjustAvatar(avatar)

        val time: String
            get() {
                if (timeText.isNotEmpty() && timeText.contains("•")) {
                    return timeText.split("•")[0].trim()
                }
                return timeText
            }

        val tagId: String?
            get() {
                if (tagLink.isEmpty()) return null
                return tagLink.substring(tagLink.lastIndexOf("/") + 1)
            }

        override fun toString(): String {
            return "Item(title='$title', linkPath='$linkPath', avatar='$avatar', avatarLink='$avatarLink', userName='$userName', timeText='$timeText', tagName='$tagName', tagLink='$tagLink', replies=$replies)"
        }
    }
}
