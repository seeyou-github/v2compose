package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * https://www.v2ex.com/my/following?p=1
 */
@Pulp("div#Wrapper")
class MyFollowingInfo : BaseInfo() {
    @Pick(value = "input.page_input", attr = "max")
    var totalPageCount: Int = 0

    @Pick("div.cell.item")
    var items: List<Item> = listOf()

    override fun isValid(): Boolean {
        if (items.isEmpty()) return true
        return items[0].userName.isNotEmpty()
    }

    override fun toString(): String {
        return "MyFollowingInfo(totalPageCount=$totalPageCount, items=$items)"
    }

    @Pulp
    class Item {
        @Pick(value = "img.avatar", attr = Attrs.SRC)
        var avatar: String = ""

        @Pick("strong a[href^=/member/]")
        var userName: String = ""

        @Pick(value = "span[title]", attr = Attrs.OWN_TEXT)
        var time: String = ""

        @Pick("span.item_title a[href^=/t/]")
        var title: String = ""

        @Pick(value = "span.item_title a[href^=/t/]", attr = Attrs.HREF)
        var link: String = ""

        @Pick("a[class^=count_]")
        var commentNum: Int = 0

        @Pick("a.node")
        var tagTitle: String = ""

        @Pick(value = "a.node", attr = Attrs.HREF)
        var tagLink: String = ""

        var _id: String = ""
        var _tagName: String = ""

        fun getId(): String {
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

        fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

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
