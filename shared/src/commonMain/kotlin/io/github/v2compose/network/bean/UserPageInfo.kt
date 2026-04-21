package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class UserPageInfo(
    @property:Pick("h1")
    val userName: String = "",
    @property:Pick(value = "img.avatar", attr = Attrs.SRC)
    val avatar: String = "",
    @property:Pick("td[valign=top] > span.gray")
    val desc: String = "",
    @property:Pick("strong.online")
    val online: String = "",
    @property:Pick(value = "div.fr input", attr = "onclick")
    val followOnClick: String = "",
    @property:Pick(value = "div.fr input[value*=lock]", attr = "onclick")
    val blockOnClick: String = "",
) {
    fun hadFollowed(): Boolean = followOnClick.contains("取消")

    fun hadBlocked(): Boolean = blockOnClick.contains("unblock")

    fun getFollowUrl(): String? = extractUrl(followOnClick)

    fun getBlockUrl(): String? = extractUrl(blockOnClick)

    fun isOnline(): Boolean = online == "ONLINE"

    fun getAdjustedAvatar(): String = avatar

    fun isValid(): Boolean = userName.isNotEmpty()

    private fun extractUrl(value: String?): String? {
        if (value.isNullOrEmpty()) return null
        val prefix = "{ location.href = '"
        val start = value.indexOf(prefix) + prefix.length
        val end = value.lastIndexOf("'")
        return if (start in 0 until end) value.substring(start, end) else null
    }
}
