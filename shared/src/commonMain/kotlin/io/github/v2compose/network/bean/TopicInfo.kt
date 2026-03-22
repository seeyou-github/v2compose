package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp
class TopicInfo : BaseInfo() {
    @Pick("div#Wrapper")
    var headerInfo: HeaderInfo? = null

    @Pick("div.content div.box")
    var contentInfo: ContentInfo? = null

    @Pick("div.problem")
    var problem: Problem? = null

    @Pick("div[id^=r_]")
    var replies: List<Reply> = listOf()

    @Pick(value = "input[name=once]", attr = "value")
    var once: String = ""

    @Pick(value = "meta[property=og:url]", attr = "content")
    var topicLink: String = ""

    @Pick(value = "a[onclick*=/report/topic/]", attr = "onclick")
    var reportLink: String = ""

    @Pick(value = "div#Wrapper div.box div.inner span.fade")
    var hasRePortStr: String = ""

    @Pick(value = "a[onclick*=/fade/topic/]", attr = "onclick")
    var fadeStr: String = ""

    @Pick(value = "a[onclick*=/sticky/topic/]", attr = "onclick")
    var stickyStr: String = ""

    fun canSticky(): Boolean = stickyUrl().isNotEmpty()
    fun canFade(): Boolean = fadeUrl().isNotEmpty()

    fun hasReported(): Boolean = hasRePortStr.isNotEmpty() && hasRePortStr.contains("已对本主题进行了报告")

    fun hasReportPermission(): Boolean = hasReported() || reportLink.isNotEmpty()

    fun fadeUrl(): String {
        if (fadeStr.isEmpty()) return ""
        var sIndex = fadeStr.indexOf("/fade/topic/")
        var eIndex = fadeStr.lastIndexOf("'")
        return if (sIndex >= 0 && eIndex > sIndex) fadeStr.substring(sIndex, eIndex) else ""
    }

    fun stickyUrl(): String {
        if (stickyStr.isEmpty()) return ""
        var sIndex = stickyStr.indexOf("/sticky/topic/")
        var eIndex = stickyStr.lastIndexOf("'")
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
    class Problem {
        @Pick(attr = Attrs.OWN_TEXT)
        var title: String = ""

        @Pick("ul li")
        var tips: List<String> = listOf()

        fun isEmpty(): Boolean = tips.isEmpty() && title.isEmpty()

        override fun toString(): String {
            return "Problem(title='$title', tips=$tips)"
        }
    }

    @Pulp
    class ContentInfo : BaseInfo() {
        @Pick(attr = Attrs.HTML)
        var html: String = ""

        @Pick(value = "div.cell div.topic_content", attr = Attrs.HTML)
        var content: String = ""

        @Pick("div.subtle")
        var supplements: List<Supplement> = listOf()

        private var _formattedHtml: String? = null
        
        fun formattedHtml(): String? {
            if (_formattedHtml != null) return _formattedHtml
            if (html.isEmpty()) return null
            // 用正则移除 class="header" 和 class="inner" 的 div
            var cleaned = html
                .replace(Regex("""(?s)<div[^>]*class="header"[^>]*>.*?</div>"""), "")
                .replace(Regex("""(?s)<div[^>]*class="inner"[^>]*>.*?</div>"""), "")
                .trim()
            // 检查是否有实际内容
            var textOnly = cleaned.replace(Regex("<[^>]*>"), "").trim()
            var hasVideo = cleaned.contains("embedded_video_wrapper")
            return if (textOnly.isEmpty() && !hasVideo) {
                null
            } else {
                _formattedHtml = cleaned
                _formattedHtml
            }
        }

        override fun isValid(): Boolean = !formattedHtml().isNullOrEmpty()

        @Pulp
        class Supplement {
            @Pick("span.fade")
            var title: String = ""

            @Pick(value = "div.topic_content", attr = Attrs.HTML)
            var content: String = ""
        }
    }

    @Pulp
    class HeaderInfo() : BaseInfo() {
        @Pick(value = "div.box img.avatar", attr = "src")
        var avatar: String = ""

        @Pick("div.box small.gray a")
        var userName: String = ""

        @Pick(value = "div.box small.gray", attr = "ownText")
        var timeText: String = ""

        @Pick("div.box a[href^=/go]")
        var tag: String = ""

        @Pick(value = "div.box a[href^=/go]", attr = Attrs.HREF)
        var tagLink: String = ""

        @Pick("div.cell span.gray:contains(回复)")
        var comment: String = ""

        @Pick("div.box div.inner a.page_normal:last-of-type")
        var page: Int = 0

        @Pick("div.box div.inner span.page_current")
        var currentPage: Int = 0

        @Pick("div.box h1")
        var title: String = ""

        @Pick(value = "div.content div.box:first-child div.inner span:first-child")
        var favoriteText: String = ""

        @Pick(value = "div.box a[href*=favorite/]", attr = Attrs.HREF)
        var favoriteLink: String = ""

        @Pick(value = "div.box a[onclick*=ignore/]", attr = "onclick")
        var ignoreLink: String = ""

        @Pick("div.box div[id=topic_thank]")
        var thankedText: String = ""

        @Pick("div.box div.inner div#topic_thank")
        var canSendThanksText: String = ""

        @Pick("div.box div.header a.op")
        var appendTxt: String = ""

        var _commentNum: String = ""
        var _tagName: String = ""
        var _time: String = ""

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
                var countStr = timeText.split("·")[1].trim()
                countStr.substring(0, countStr.indexOf(" ")).toInt()
            } catch (e: Exception) {
                0
            }
        }

        fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

        fun getTotalPage(): Int = maxOf(maxOf(page, currentPage), 1)

        override fun toString(): String {
            return "HeaderInfo(userName='$userName', title='$title', favoriteCount=${getFavoriteCount()})"
        }
    }

    @Pulp
    class Reply {
        @Pick(value = "div.reply_content", attr = Attrs.HTML)
        var replyContent: String = ""

        @Pick("strong a.dark[href^=/member]")
        var userName: String = ""

        @Pick(value = "img.avatar", attr = "src")
        var avatar: String = ""

        @Pick("span.fade.small:not(:contains(♥))")
        var time: String = ""

        @Pick("span.small.fade:has(img)")
        var thanksText: String = ""

        @Pick("span.no")
        var floor: Int = 0

        @Pick("div.thank_area.thanked")
        var alreadyThanked: String = ""

        @Pick(attr = "id")
        var replyIdText: String = ""

        fun replyId(): String = replyIdText.substringAfter("_", "")

        fun hadThanked(): Boolean = alreadyThanked.isNotEmpty()

        fun getAdjustedAvatar(): String = avatar // TODO: AvatarUtils 迁移后恢复

        fun thanksCount(): Int = thanksText.toIntOrNull() ?: 0

        override fun toString(): String {
            return "Reply(userName='$userName', floor=$floor, thanksCount=${thanksCount()})"
        }
    }
}
