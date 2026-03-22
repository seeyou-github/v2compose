package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class HomePageInfo : BaseInfo() {
    @Pick("h1")
    var userName: String = ""

    @Pick(value = "img.avatar", attr = Attrs.SRC)
    var avatar: String = ""

    @Pick("td[valign=top] > span.gray")
    var desc: String = ""

    @Pick("strong.online")
    var online: String = ""

    @Pick("a[href=/my/nodes] span.bigger")
    var nodes: Int = 0

    @Pick("a[href=/my/topics] span.bigger")
    var topics: Int = 0

    @Pick("a[href=/my/following] span.bigger")
    var following: Int = 0

    fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

    fun isOnline(): Boolean = online.isNotEmpty() && online == "ONLINE"

    override fun toString(): String {
        return "HomePageInfo(userName='$userName', avatar='$avatar', desc='$desc', online='$online')"
    }

    override fun isValid(): Boolean {
        return userName.isNotEmpty()
    }
}
