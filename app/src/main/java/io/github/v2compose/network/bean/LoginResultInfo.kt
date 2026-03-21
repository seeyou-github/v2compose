package io.github.v2compose.network.bean

import io.github.v2compose.util.Check
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * Created by ghui on 16/08/2017.
 */
@Pulp("header#site-header")
class LoginResultInfo : BaseInfo() {
    @Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = ""

    @Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = ""

    override fun isValid(): Boolean {
        return avatar.isNotEmpty()
    }

    override fun toString(): String {
        return "LoginResultInfo(userLink='$userLink', avatar='$avatar')"
    }

    val userName: String?
        get() {
            if (userLink.isEmpty()) return null
            return userLink.split("/").getOrNull(2)
        }

    val largeAvatar: String?
        get() {
            if (avatar.isEmpty()) return null
            return avatar.replace("normal.png", "large.png")
        }
}
