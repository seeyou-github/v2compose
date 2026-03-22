package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
class NotificationInfo : BaseInfo() {
    @Pick("div#Main div.box div.fr.f12 strong")
    var total: Int = 0

    @Pick("div#Main div.box div.cell[id^=n_]")
    var replies: List<Reply> = listOf()

    @Pick("div#Rightbar div.box a[href*=notifications]")
    var unread: String = ""

    fun unreadCount(): Int {
        if (unread.isEmpty()) return 0
        return try {
            unread.split(" ").getOrNull(0)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    override fun isValid(): Boolean {
        if (replies.isEmpty()) return true
        return replies[0].name.isNotEmpty()
    }

    override fun toString(): String {
        return "NotificationInfo(total=$total, replies=$replies)"
    }

    @Pulp
    class Reply {
        @Pick(value = "div.cell[id^=n_]", attr = "id")
        var idText: String = ""

        @Pick("a[href^=/member/] strong")
        var name: String = ""

        @Pick(value = "a[href^=/member/] img", attr = Attrs.SRC)
        var avatar: String = ""

        @Pick(value = "span.fade")
        var titleText: String = ""

        @Pick(value = "a[href^=/t/]", attr = Attrs.HREF)
        var link: String = ""

        @Pick(value = "div.payload", attr = Attrs.HTML)
        var content: String = ""

        @Pick("span.snow")
        var time: String = ""

        fun getId(): String = if (idText.length > 2) idText.substring(2) else ""

        fun getFullLink(): String = "https://www.v2ex.com$link"

        fun getAdjustedTitle(): String =
            if (titleText.isNotEmpty()) titleText.replaceFirst(name, "").trim() else ""

        fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

        override fun toString(): String {
            return "Reply(name='$name', time='$time')"
        }
    }
}
