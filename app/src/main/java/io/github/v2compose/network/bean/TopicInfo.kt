package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import io.github.v2compose.util.AvatarUtils
import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import org.jsoup.Jsoup
import java.io.Serializable


@Stable
@Pulp
class TopicInfo : BaseInfo() {
    @Pick("div#Wrapper")
    val headerInfo: HeaderInfo? = null

    @Pick("div.content div.box")
    val contentInfo: ContentInfo? = null

    @Pick("div.problem")
    val problem: Problem? = null

    @Pick("div[id^=r_]")
    val replies: List<Reply> = listOf()

    @Pick(value = "input[name=once]", attr = "value")
    val once: String = ""

    @Pick(value = "meta[property=og:url]", attr = "content")
    val topicLink: String = ""

    @Pick(value = "a[onclick*=/report/topic/]", attr = "onclick")
    private val reportLink: String = ""

    @Pick(value = "div#Wrapper div.box div.inner span.fade")
    private val hasRePortStr: String = ""

    @Pick(value = "a[onclick*=/fade/topic/]", attr = "onclick")
    private val fadeStr: String = ""

    @Pick(value = "a[onclick*=/sticky/topic/]", attr = "onclick")
    private val stickyStr: String = ""

    fun canSticky(): Boolean = stickyUrl().isNotEmpty()
    fun canFade(): Boolean = fadeUrl().isNotEmpty()

    fun hasReported(): Boolean = hasRePortStr.isNotEmpty() && hasRePortStr.contains("已对本主题进行了报告")

    fun hasReportPermission(): Boolean = hasReported() || reportLink.isNotEmpty()

    fun fadeUrl(): String {
        if (fadeStr.isEmpty()) return ""
        val sIndex = fadeStr.indexOf("/fade/topic/")
        val eIndex = fadeStr.lastIndexOf("'")
        return if (sIndex >= 0 && eIndex > sIndex) fadeStr.substring(sIndex, eIndex) else ""
    }

    fun stickyUrl(): String {
        if (stickyStr.isEmpty()) return ""
        val sIndex = stickyStr.indexOf("/sticky/topic/")
        val eIndex = stickyStr.lastIndexOf("'")
        return if (sIndex >= 0 && eIndex > sIndex) stickyStr.substring(sIndex, eIndex) else ""
    }

    fun totalPage(): Int = headerInfo?.getTotalPage() ?: 0

    fun toReplyMap(content: String): Map<String, String> {
        return mapOf("once" to once, "content" to content)
    }

    override fun toString(): String {
        return "TopicInfo(topicLink='$topicLink', headerInfo=$headerInfo, repliesCount=${replies.size}, once='$once')"
    }

    override fun isValid(): Boolean {
        return headerInfo?.isValid() == true
    }

    @Pulp
    class Problem : Serializable {
        @Pick(attr = Attrs.OWN_TEXT)
        val title: String = ""

        @Pick("ul li")
        val tips: List<String> = listOf()

        fun isEmpty(): Boolean = tips.isEmpty() && title.isEmpty()

        override fun toString(): String {
            return "Problem(title='$title', tips=$tips)"
        }
    }

    @Pulp
    class ContentInfo : BaseInfo() {
        @Pick(attr = Attrs.HTML)
        private val html: String = ""

        @Pick(value = "div.cell div.topic_content", attr = Attrs.HTML)
        val content: String = ""

        @Pick("div.subtle")
        val supplements: List<Supplement> = listOf()

        private var formattedHtml: String? = null

        fun getFormattedHtml(): String? {
            if (formattedHtml != null) return formattedHtml
            if (html.isEmpty()) return null
            val doc = Jsoup.parse(html)
            doc.getElementsByClass("header").remove()
            doc.getElementsByClass("inner").remove()
            val text = doc.text().trim()
            val hasVideo = doc.getElementsByClass("embedded_video_wrapper").isNotEmpty()
            return if (text.isEmpty() && !hasVideo) {
                null
            } else {
                formattedHtml = doc.body().html()
                formattedHtml
            }
        }

        override fun isValid(): Boolean = !getFormattedHtml().isNullOrEmpty()

        @Pulp
        class Supplement : Serializable {
            @Pick("span.fade")
            val title: String = ""

            @Pick(value = "div.topic_content", attr = Attrs.HTML)
            val content: String = ""
        }
    }

    @Pulp
    class HeaderInfo() : BaseInfo() {
        @Pick(value = "div.box img.avatar", attr = "src")
        val avatar: String = ""

        @Pick("div.box small.gray a")
        val userName: String = ""

        @Pick(value = "div.box small.gray", attr = "ownText")
        private val timeText: String = ""

        @Pick("div.box a[href^=/go]")
        val tag: String = ""

        @Pick(value = "div.box a[href^=/go]", attr = Attrs.HREF)
        val tagLink: String = ""

        @Pick("div.cell span.gray:contains(回复)")
        private val comment: String = ""

        @Pick("div.box div.inner a.page_normal:last-of-type")
        val page: Int = 0

        @Pick("div.box div.inner span.page_current")
        val currentPage: Int = 0

        @Pick("div.box h1")
        val title: String = ""

        @Pick(value = "div.content div.box:first-child div.inner span:first-child")
        private val favoriteText: String = ""

        @Pick(value = "div.box a[href*=favorite/]", attr = Attrs.HREF)
        val favoriteLink: String = ""

        @Pick(value = "div.box a[onclick*=ignore/]", attr = "onclick")
        private val ignoreLink: String = ""

        @Pick("div.box div[id=topic_thank]")
        private val thankedText: String = ""

        @Pick("div.box div.inner div#topic_thank")
        private val canSendThanksText: String = ""

        @Pick("div.box div.header a.op")
        private val appendTxt: String = ""

        private var _commentNum: String = ""
        private var _tagName: String = ""
        private var _time: String = ""
        private var _avatar: String = ""

        override fun isValid(): Boolean = userName.isNotEmpty() && tag.isNotEmpty()

        fun canAppend(): Boolean = appendTxt.isNotEmpty() && appendTxt == "APPEND"
        fun canSendThanks(): Boolean = canSendThanksText.isNotEmpty()
        fun hadThanked(): Boolean = thankedText.isNotEmpty() && thankedText.contains("已发送")
        fun hadFavorited(): Boolean =
            favoriteLink.isNotEmpty() && favoriteLink.contains("unfavorite/")

        fun hadIgnored(): Boolean = ignoreLink.isNotEmpty() && ignoreLink.contains("unignore/")

        fun getFavoriteCount(): Int {
            if (favoriteText.isEmpty()) return 0
            return try {
                favoriteText.trim().split(" ").getOrNull(0)?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }
        }

        fun getCommentNum(): String {
            if (_commentNum.isNotEmpty()) return _commentNum
            _commentNum = if (comment.isEmpty()) "" else comment.split(" ")[0]
            return _commentNum
        }

        fun getTagName(): String {
            if (_tagName.isNotEmpty()) return _tagName
            _tagName = tagLink.replace("/go/", "")
            return _tagName
        }

        fun getTime(): String {
            if (_time.isNotEmpty()) return _time
            return try {
                if (timeText.isNotEmpty() && timeText.contains("·")) {
                    var temp = timeText.split("·")[0].trim().substring(6).replace(" ", "").trim()
                    if (temp.contains("-") && temp.contains("+")) {
                        temp = temp.substring(0, 10)
                    }
                    _time = temp
                }
                _time
            } catch (e: Exception) {
                ""
            }
        }

        fun getViewCount(): Int {
            return try {
                val countStr = timeText.split("·")[1].trim()
                countStr.substring(0, countStr.indexOf(" ")).toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun getAdjustedAvatar(): String {
            if (_avatar.isNotEmpty()) return _avatar
            _avatar = AvatarUtils.adjustAvatar(avatar)
            return _avatar
        }

        fun getTotalPage(): Int = maxOf(maxOf(page, currentPage), 1)

        override fun toString(): String {
            return "HeaderInfo(userName='$userName', title='$title', favoriteCount=${getFavoriteCount()})"
        }
    }

    @Stable
    @Pulp
    class Reply : Serializable {
        @Pick(value = "div.reply_content", attr = Attrs.HTML)
        val replyContent: String = ""

        @Pick("strong a.dark[href^=/member]")
        val userName: String = ""

        @Pick(value = "img.avatar", attr = "src")
        val avatar: String = ""

        @Pick("span.fade.small:not(:contains(♥))")
        val time: String = ""

        @Pick("span.small.fade:has(img)")
        val thanksText: String = ""

        @Pick("span.no")
        val floor: Int = 0

        @Pick("div.thank_area.thanked")
        val alreadyThanked: String = ""

        @Pick(attr = "id")
        val replyIdText: String = ""

        fun replyId(): String = replyIdText.substringAfter("_", "")

        fun hadThanked(): Boolean = alreadyThanked.isNotEmpty()

        fun getAdjustedAvatar(): String = AvatarUtils.adjustAvatar(avatar)

        fun thanksCount(): Int = thanksText.toIntOrNull() ?: 0

        override fun toString(): String {
            return "Reply(userName='$userName', floor=$floor, thanksCount=${thanksCount()})"
        }
    }
}
