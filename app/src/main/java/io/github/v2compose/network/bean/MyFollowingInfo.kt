package io.github.v2compose.network.bean

import io.github.v2compose.util.AvatarUtils
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import java.io.Serializable

/**
 * Created by ghui on 12/05/2017.
 * https://www.v2ex.com/my/following?p=1
 */
@Pulp("div#Wrapper")
class MyFollowingInfo : BaseInfo() {
    @Pick(value = "input.page_input", attr = "max")
    val totalPageCount: Int = 0

    @Pick("div.cell.item")
    val items: List<Item> = listOf()

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].userName.isNotEmpty()
    }

    override fun toString(): String {
        return "MyFollowingInfo(totalPageCount=$totalPageCount, items=$items)"
    }

    @Pulp
    class Item : Serializable {
        @Pick(value = "img.avatar", attr = Attrs.SRC)
        val avatar: String = ""

        @Pick("strong a[href^=/member/]")
        val userName: String = ""

        @Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        val time: String = ""

        @Pick("span.item_title a[href^=/t/]")
        val title: String = ""

        @Pick(value = "span.item_title a[href^=/t/]", attr = Attrs.HREF)
        val link: String = ""

        @Pick("a[class^=count_]")
        val commentNum: Int = 0

        @Pick("a.node")
        val tagTitle: String = ""

        @Pick(value = "a.node", attr = Attrs.HREF)
        val tagLink: String = ""

        private var _id: String = ""
        private var _avatar: String = ""
        private var _tagName: String = ""

        fun getId(): String {
            if (_id.isNotEmpty()) return _id
            if (link.startsWith("/t/")) {
                val end = link.indexOf('#')
                _id = if (end > 0) {
                    link.substring("/t/".length, end)
                } else {
                    link.substring("/t/".length)
                }
            }
            return _id
        }

        fun getAdjustedAvatar(): String {
            if (_avatar.isNotEmpty()) return _avatar
            _avatar = AvatarUtils.adjustAvatar(avatar)
            return _avatar
        }

        fun getTagName(): String {
            if (_tagName.isNotEmpty()) return _tagName
            if (tagLink.startsWith("/go/")) {
                _tagName = tagLink.substring("/go/".length)
            }
            return _tagName
        }

        override fun toString(): String {
            return "Item(userName='$userName', title='$title', link='$link')"
        }
    }
}
