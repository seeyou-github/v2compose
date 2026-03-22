package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp


@Pulp("header#site-header")
class LoginResultInfo : BaseInfo() {
    @Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = ""

    @Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = ""

    override fun isValid(): Boolean = avatar.isNotEmpty()

    override fun toString(): String {
        return "LoginResultInfo(userLink='$userLink', avatar='$avatar')"
    }

    private var _userName: String? = null
    val userName: String?
        get() {
            if (_userName != null) return _userName
            if (userLink.isEmpty()) return null
            _userName = userLink.split("/").getOrNull(2)
            return _userName
        }

}
