package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

@Slice
data class TwoStepLoginInfo(
    @property:Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = "",
    @property:Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = "",
    @property:Pick(value = "div.problem", attr = Attrs.HTML)
    val problem: String = "",
    @property:Pick("tr:first-child")
    val title: String = "",
    @property:Pick(value = "input[type=hidden]", attr = "value")
    val once: String = "",
) {
    val userName: String?
        get() = userLink.split("/").getOrNull(2)

    val largeAvatar: String?
        get() = avatar.takeIf { it.isNotEmpty() }?.replace("normal.png", "large.png")

    fun isValid(): Boolean {
        return avatar.isNotEmpty() && once.isNotEmpty() && title.contains("两步验证")
    }
}
