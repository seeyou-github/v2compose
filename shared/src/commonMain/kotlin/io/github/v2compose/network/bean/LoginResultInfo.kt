package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("header#site-header")
data class LoginResultInfo(
    @property:Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = "",
    @property:Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = "",
) {
    val userName: String?
        get() = userLink.split("/").getOrNull(2)

    fun isValid(): Boolean = avatar.isNotEmpty()
}
