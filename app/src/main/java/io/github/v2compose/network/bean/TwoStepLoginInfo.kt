package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * Created by ghui on 16/08/2017.
 */
@Pulp
class TwoStepLoginInfo : BaseInfo() {
    @Pick(value = "[href^=/member]", attr = "href")
    private val userLink: String = ""

    @Pick(value = "img[src*=avatar/]", attr = "src")
    private val avatar: String = ""

    @Pick(value = "div.problem", attr = Attrs.HTML)
    val problem: String = ""

    @Pick("tr:first-child")
    val title: String = ""

    @Pick(value = "input[type=hidden]", attr = "value")
    val once: String = ""

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

    override fun isValid(): Boolean {
        return avatar.isNotEmpty() && once.isNotEmpty() && title.isNotEmpty() && title.contains("两步验证")
    }

    override fun toString(): String {
        return "TwoStepLoginInfo(userLink='$userLink', avatar='$avatar', problem='$problem', title='$title', once='$once')"
    }
}
