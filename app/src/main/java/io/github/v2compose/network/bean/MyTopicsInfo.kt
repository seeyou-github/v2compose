package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import io.github.v2compose.util.AvatarUtils
import java.io.Serializable

/**
 * https://www.v2ex.com/my/topics
 */
@Pulp("div#Wrapper")
class MyTopicsInfo : BaseInfo() {
    @Pick(value = "input.page_input", attr = "max")
    private val totalPageCountText: String = ""

    @Pick("div.cell.item")
    val items: List<Item> = listOf()

    fun totalPageCount(): Int = totalPageCountText.toIntOrNull() ?: 0

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].title.isNotEmpty()
    }

    override fun toString(): String {
        return "MyTopicsInfo(totalPageCountText='$totalPageCountText', items=$items)"
    }

    @Pulp
    class Item : Serializable {
        @Pick(value = "td>a[href^=/member]", attr = Attrs.HREF)
        val userLink: String = ""

        @Pick(value = "img.avatar", attr = Attrs.SRC)
        val avatar: String = ""

        @Pick("span.item_title")
        val title: String = ""

        @Pick(value = "span.item_title a", attr = Attrs.HREF)
        val link: String = ""

        @Pick("a[class^=count_]")
        val commentNum: Int = 0

        @Pick("a.node")
        val tagTitle: String = ""

        @Pick(value = "a.node", attr = Attrs.HREF)
        val tagLink: String = ""

        @Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        val time: String = ""

        private var _id: String = ""
        private var _userName: String = ""
        private var _avatar: String = ""
        private var _tagName: String = ""

        val id: String
            get() {
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

        val userName: String
            get() {
                if (_userName.isNotEmpty()) return _userName
                if (userLink.isNotEmpty()) {
                    _userName = userLink.substring(userLink.lastIndexOf("/") + 1)
                }
                return _userName
            }

        val adjustedAvatar: String
            get() {
                if (_avatar.isNotEmpty()) return _avatar
                if (avatar.isNotEmpty()) {
                    _avatar = AvatarUtils.adjustAvatar(avatar)
                }
                return _avatar
            }

        val tagName: String
            get() {
                if (_tagName.isNotEmpty()) return _tagName
                if (tagLink.startsWith("/go/")) {
                    _tagName = tagLink.substring("/go/".length)
                }
                return _tagName
            }

        override fun toString(): String {
            return "Item(userLink='$userLink', avatar='$avatar', title='$title', link='$link', commentNum=$commentNum, tagTitle='$tagTitle', tagLink='$tagLink', time='$time')"
        }
    }
}
