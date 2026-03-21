package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import io.github.v2compose.util.AvatarUtils
import io.github.v2compose.util.Check
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * Created by ghui on 01/06/2017.
 * https://www.v2ex.com/member/ghui
 */
@Stable
@Pulp("div#Wrapper")
class UserPageInfo : BaseInfo() {
    @Pick("h1")
    val userName: String = ""

    @Pick(value = "img.avatar", attr = Attrs.SRC)
    val avatar: String = ""

    @Pick("td[valign=top] > span.gray")
    val desc: String = ""

    @Pick("strong.online")
    val online: String = ""

    @Pick(value = "div.fr input", attr = "onclick")
    val followOnClick: String = ""

    @Pick(value = "div.fr input[value*=lock]", attr = "onclick")
    val blockOnClick: String = ""

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

    private fun getUrl(onclick: String?): String? {
        if (!onclick.isNullOrEmpty()) {
            val reg = "{ location.href = '"
            val start = onclick.indexOf(reg) + reg.length
            val end = onclick.lastIndexOf("'")
            if (start in 0 until end) {
                return onclick.substring(start, end)
            }
        }
        return null
    }

    val isOnline: Boolean
        get() = online.isNotEmpty() && online == "ONLINE"

    val adjustedAvatar: String
        get() = AvatarUtils.adjustAvatar(avatar)

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
