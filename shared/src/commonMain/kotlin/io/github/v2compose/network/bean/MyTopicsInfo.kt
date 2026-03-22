package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/my/topics
 */
@Pulp("div#Wrapper")
class MyTopicsInfo : BaseInfo() {
    @Pick(value = "input.page_input", attr = "max")
    var totalPageCountText: String = ""

    @Pick("div.cell.item")
    var items: List<Item> = listOf()

    fun totalPageCount(): Int = totalPageCountText.toIntOrNull() ?: 0

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].title.isNotEmpty()
    }

    override fun toString(): String {
        return "MyTopicsInfo(totalPageCountText='$totalPageCountText', items=$items)"
    }

    @Pulp
    class Item {
        @Pick(value = "td>a[href^=/member]", attr = Attrs.HREF)
        var userLink: String = ""

        @Pick(value = "img.avatar", attr = Attrs.SRC)
        var avatar: String = ""

        @Pick("span.item_title")
        var title: String = ""

        @Pick(value = "span.item_title a", attr = Attrs.HREF)
        var link: String = ""

        @Pick("a[class^=count_]")
        var commentNum: Int = 0

        @Pick("a.node")
        var tagTitle: String = ""

        @Pick(value = "a.node", attr = Attrs.HREF)
        var tagLink: String = ""

        @Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        var time: String = ""

        var _id: String = ""
        var _userName: String = ""
        var _tagName: String = ""

        val id: String
            get() {
                if (_id.isNotEmpty()) return _id
                if (link.startsWith("/t/")) {
                    var end = link.indexOf('#')
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
            get() = avatar // TODO: AvatarUtils 迁移后恢复

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
