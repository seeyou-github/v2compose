package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/go/python
 */
@Pulp("div#Wrapper")
class NodeTopicInfo : BaseInfo() {
    @Pick("span.topic-count strong")
    var totalText: String = ""

    @Pick(value = "a[href*=favorite/] ", attr = Attrs.HREF)
    var favoriteLink: String = ""

    @Pick("div.box div.cell:has(table)")
    var items: List<Item> = listOf()

    fun total(): Int {
        if (totalText.isEmpty()) return 0
        return try {
            totalText.replace(",", "").toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    val fullFavoriteLink: String
        get() = "https://www.v2ex.com$favoriteLink"

    val hasStared: Boolean
        get() = favoriteLink.isNotEmpty() && favoriteLink.contains("/unfavorite/node/")

    val once: String?
        get() {
            if (favoriteLink.isEmpty()) return null
            var regex = Regex("once=([^&]+)")
            return regex.find(favoriteLink)?.groupValues?.getOrNull(1)
        }

    override fun toString(): String {
        return "NodeTopicInfo(favoriteLink='$favoriteLink', totalText='$totalText', items=$items)"
    }

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].userName.isNotEmpty()
    }

    @Pulp
    class Item {
        @Pick(value = "img.avatar", attr = Attrs.SRC)
        var avatar: String = ""

        @Pick("span.item_title")
        var title: String = ""

        @Pick("span.small.fade strong")
        var userName: String = ""

        @Pick(value = "span.small.fade", attr = Attrs.OWN_TEXT)
        var clickedAndContentLength: String = ""

        @Pick("a[class^=count_]")
        var commentNum: Int = 0

        @Pick(value = "span.item_title a", attr = Attrs.HREF)
        var topicLinkText: String = ""

        val topicId: String
            get() {
                var end = topicLinkText.indexOf('#')
                return if (end > 3) topicLinkText.substring(3, end) else ""
            }

        val topicLink: String
            get() = topicLinkText

        val avatarUrl: String
            get() = if (avatar.isNotEmpty() && avatar.startsWith("http")) avatar else "https:$avatar"

        val clickNum: Int
            get() {
                if (clickedAndContentLength.isEmpty()) return 0
                return try {
                    var result =
                        clickedAndContentLength.substring(clickedAndContentLength.lastIndexOf("•") + 1)
                    result.replace("[^0-9]".toRegex(), "").toInt()
                } catch (e: Exception) {
                    0
                }
            }

        val contentLength: Int
            get() {
                if (clickedAndContentLength.isEmpty()) return 0
                return try {
                    var trimmed = clickedAndContentLength.trim()
                    var result = trimmed.substring(0, trimmed.lastIndexOf("•")).trim()
                    result.split(" ")[1].trim().toInt()
                } catch (e: Exception) {
                    0
                }
            }
    }
}
