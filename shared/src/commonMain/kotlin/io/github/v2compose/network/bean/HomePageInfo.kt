package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

@Slice("div#Wrapper")
data class HomePageInfo(
    @property:Pick("h1")
    val userName: String = "",
    @property:Pick(value = "img.avatar", attr = Attrs.SRC)
    val avatar: String = "",
    @property:Pick("td[valign=top] > span.gray")
    val desc: String = "",
    @property:Pick("strong.online")
    val online: String = "",
    @property:Pick("a[href=/my/nodes] span.bigger")
    val nodes: Int = 0,
    @property:Pick("a[href=/my/topics] span.bigger")
    val topics: Int = 0,
    @property:Pick("a[href=/my/following] span.bigger")
    val following: Int = 0,
) {
    fun getAdjustedAvatar(): String = avatar

    fun isOnline(): Boolean = online == "ONLINE"

    fun isValid(): Boolean = userName.isNotEmpty()
}
