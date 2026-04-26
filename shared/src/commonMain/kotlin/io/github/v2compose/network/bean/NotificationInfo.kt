package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

@Slice("div#Wrapper")
data class NotificationInfo(
    @property:Pick("div#Main div.box div.fr.f12 strong")
    val total: Int = 0,
    @property:Pick("div#Main div.box div.cell[id^=n_]")
    val replies: List<Reply> = emptyList(),
    @property:Pick("div#Rightbar div.box a[href*=notifications]")
    val unread: String = "",
) {
    fun unreadCount(): Int = unread.split(" ").getOrNull(0)?.toIntOrNull() ?: 0

    fun isValid(): Boolean = replies.isEmpty() || replies[0].name.isNotEmpty()

    @Slice
    data class Reply(
        @property:Pick(value = "div.cell[id^=n_]", attr = "id")
        val idText: String = "",
        @property:Pick("a[href^=/member/] strong")
        val name: String = "",
        @property:Pick(value = "a[href^=/member/] img", attr = Attrs.SRC)
        val avatar: String = "",
        @property:Pick(value = "span.fade")
        val titleText: String = "",
        @property:Pick(value = "a[href^=/t/]", attr = Attrs.HREF)
        val link: String = "",
        @property:Pick(value = "div.payload", attr = Attrs.HTML)
        val content: String = "",
        @property:Pick("span.snow")
        val time: String = "",
    ) {
        fun getId(): String = if (idText.length > 2) idText.substring(2) else ""

        fun getFullLink(): String = "https://www.v2ex.com$link"

        fun getAdjustedTitle(): String {
            return if (titleText.isNotEmpty()) titleText.replaceFirst(name, "").trim() else ""
        }

        fun getAdjustedAvatar(): String = avatar
    }
}
