package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class UserPageInfo : BaseInfo() {
    @Pick("h1")
    var userName: String = ""

    @Pick(value = "img.avatar", attr = Attrs.SRC)
    var avatar: String = ""

    @Pick("td[valign=top] > span.gray")
    var desc: String = ""

    @Pick("strong.online")
    var online: String = ""

    @Pick(value = "div.fr input", attr = "onclick")
    var followOnClick: String = ""

    @Pick(value = "div.fr input[value*=lock]", attr = "onclick")
    var blockOnClick: String = ""

    fun hadFollowed(): Boolean {
        return followOnClick.isNotEmpty() && followOnClick.contains("取消")
    }

    fun hadBlocked(): Boolean {
        return blockOnClick.isNotEmpty() && blockOnClick.contains("unblock")
    }

    fun getFollowUrl(): String? {
        return getUrl(followOnClick)
    }

    fun getBlockUrl(): String? {
        return getUrl(blockOnClick)
    }

    private fun getUrl(url: String?): String? {
        if (!url.isNullOrEmpty()) {
            var reg = "{ location.href = '"
            var start = url.indexOf(reg) + reg.length
            var end = url.lastIndexOf("'")
            if (start in 0 until end) {
                return url.substring(start, end)
            }
        }
        return null
    }

    fun isOnline(): Boolean = online.isNotEmpty() && online == "ONLINE"

    fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

    override fun toString(): String {
        return "UserPageInfo(" +
                "userName='$userName', " +
                "followOnClick='$followOnClick', " +
                "blockOnClick='$blockOnClick', " +
                "avatar='$avatar', " +
                "desc='$desc', " +
                "online='$online'" +
                ")"
    }

    override fun isValid(): Boolean {
        return userName.isNotEmpty()
    }
}
