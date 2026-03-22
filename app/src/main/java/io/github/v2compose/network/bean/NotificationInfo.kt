package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import io.github.v2compose.network.NetConstants
import io.github.v2compose.util.AvatarUtils
import java.io.Serializable


@Stable
@Pulp("div#Wrapper")
class NotificationInfo : BaseInfo() {
    @Pick("div#Main div.box div.fr.f12 strong")
    val total: Int = 0

    @Pick("div#Main div.box div.cell[id^=n_]")
    val replies: List<Reply> = listOf()

    @Pick("div#Rightbar div.box a[href*=notifications]")
    private val unread: String = ""

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

    @Stable
    @Pulp
    class Reply : Serializable {
        @Pick(value = "div.cell[id^=n_]", attr = "id")
        val idText: String = ""

        @Pick("a[href^=/member/] strong")
        val name: String = ""

        @Pick(value = "a[href^=/member/] img", attr = Attrs.SRC)
        val avatar: String = ""

        @Pick(value = "span.fade")
        val titleText: String = ""

        @Pick(value = "a[href^=/t/]", attr = Attrs.HREF)
        val link: String = ""

        @Pick(value = "div.payload", attr = Attrs.HTML)
        val content: String = ""

        @Pick("span.snow")
        val time: String = ""

        fun getId(): String = if (idText.length > 2) idText.substring(2) else ""

        fun getFullLink(): String = NetConstants.BASE_URL + link

        fun getAdjustedTitle(): String =
            if (titleText.isNotEmpty()) titleText.replaceFirst(name, "").trim() else ""

        fun getAdjustedAvatar(): String = AvatarUtils.adjustAvatar(avatar)

        override fun toString(): String {
            return "Reply(name='$name', time='$time')"
        }
    }
}
