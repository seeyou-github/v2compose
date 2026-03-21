package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import io.github.v2compose.network.NetConstants
import io.github.v2compose.util.UriUtils
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 27/05/2017.
 * https://www.v2ex.com/go/python
 */
@Stable
@Pulp("div#Wrapper")
class NodeTopicInfo : BaseInfo() {
    @Pick("span.topic-count strong")
    val totalText: String = ""

    @Pick(value = "a[href*=favorite/] ", attr = Attrs.HREF)
    val favoriteLink: String = ""

    @Pick("div.box div.cell:has(table)")
    val items: List<Item> = listOf()

    val total: Int
        get() {
            if (totalText.isEmpty()) return 0
            return try {
                totalText.replace(",", "").toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                0
            }
        }

    val fullFavoriteLink: String
        get() = NetConstants.BASE_URL + favoriteLink

    val hasStared: Boolean
        get() = favoriteLink.isNotEmpty() && favoriteLink.contains("/unfavorite/node/")

    val once: String?
        get() = if (favoriteLink.isNotEmpty()) UriUtils.getParamValue(favoriteLink, "once") else null

    override fun toString(): String {
        return "NodeTopicInfo(favoriteLink='$favoriteLink', totalText='$totalText', items=$items)"
    }

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].userName.isNotEmpty()
    }

    @Stable
    @Pulp
    class Item : Serializable {
        @Pick(value = "img.avatar", attr = Attrs.SRC)
        val avatar: String = ""

        @Pick("span.item_title")
        val title: String = ""

        @Pick("span.small.fade strong")
        val userName: String = ""

        @Pick(value = "span.small.fade", attr = Attrs.OWN_TEXT)
        private val clickedAndContentLength: String = ""

        @Pick("a[class^=count_]")
        val commentNum: Int = 0

        @Pick(value = "span.item_title a", attr = Attrs.HREF)
        private val topicLinkText: String = ""

        val topicId: String
            get() {
                val end = topicLinkText.indexOf('#')
                return if (end > 3) topicLinkText.substring(3, end) else ""
            }

        val topicLink: String
            get() = topicLinkText

        val avatarUrl: String
            get() = if (avatar.isNotEmpty() && avatar.startsWith("http")) avatar else NetConstants.HTTPS_SCHEME + avatar

        val clickNum: Int
            get() {
                if (clickedAndContentLength.isEmpty()) return 0
                return try {
                    val result = clickedAndContentLength.substring(clickedAndContentLength.lastIndexOf("•") + 1)
                    result.replace("[^0-9]".toRegex(), "").toInt()
                } catch (e: Exception) {
                    0
                }
            }

        val contentLength: Int
            get() {
                if (clickedAndContentLength.isEmpty()) return 0
                return try {
                    val trimmed = clickedAndContentLength.trim()
                    val result = trimmed.substring(0, trimmed.lastIndexOf("•")).trim()
                    result.split(" ")[1].trim().toInt()
                } catch (e: Exception) {
                    0
                }
            }
    }
}
