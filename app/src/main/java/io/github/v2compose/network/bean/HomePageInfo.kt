package io.github.v2compose.network.bean

import io.github.v2compose.util.AvatarUtils
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class HomePageInfo : BaseInfo() {
    @Pick("h1")
    val userName: String = ""

    @Pick(value = "img.avatar", attr = Attrs.SRC)
    private val avatar: String = ""

    @Pick("td[valign=top] > span.gray")
    val desc: String = ""

    @Pick("strong.online")
    private val online: String = ""

    @Pick("a[href=/my/nodes] span.bigger")
    val nodes: Int = 0

    @Pick("a[href=/my/topics] span.bigger")
    val topics: Int = 0

    @Pick("a[href=/my/following] span.bigger")
    val following: Int = 0

    val adjustedAvatar: String
        get() = AvatarUtils.adjustAvatar(avatar)

    val isOnline: Boolean
        get() = online.isNotEmpty() && online == "ONLINE"

    override fun toString(): String {
        return "HomePageInfo(userName='$userName', avatar='$avatar', desc='$desc', online='$online')"
    }

    override fun isValid(): Boolean {
        return userName.isNotEmpty()
    }
}
