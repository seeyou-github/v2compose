package com.imhanjie.v2ex.api.parser.impl

import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.MemberReplies
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup
import java.util.regex.Pattern
import kotlin.math.max

object MemberRepliesParser : ParserMatcher {

    override fun match(url: String, method: String): Boolean {
        return Pattern.compile("^${V2ex.BASE_URL}/member/\\w+/replies\\?p=\\d+\$").matcher(url).find()
    }

    override fun parser(html: String): Any {
        val document = Jsoup.parse(html)
        val currentPage = document.selectFirst("a.page_current")?.text()?.toIntOrNull() ?: 1
        var totalPage = currentPage
        document.select("a.page_normal").let {
            if (it.isNotEmpty()) {
                totalPage = max(it.last()?.text()?.toIntOrNull() ?: currentPage, currentPage)
            }
        }
        val totalCount = document.selectFirst("div.header")?.selectFirst("strong.gray")?.text()?.toIntOrNull() ?: 0

        val box = document.selectFirst("#Main")?.selectFirst("div.box")
        val titleElements = box?.select("div.dock_area")?.select("td") ?: emptyList()
        val replyElements = box?.select("div.reply_content") ?: emptyList()
        val replies = mutableListOf<MemberReplies.Item>()
        for (i in 0 until replyElements.size) {
            val eTitle = titleElements.getOrNull(i)
            val eReply = replyElements.getOrNull(i)
            val titleRichContent = eTitle?.selectFirst("span.gray")?.html() ?: ""
            val createTime = eTitle?.selectFirst("span.fade")?.text() ?: ""
            val replyRichContent = eReply?.html() ?: ""
            replies.add(
                MemberReplies.Item(
                    titleRichContent, createTime, replyRichContent
                )
            )
        }
        return MemberReplies(
            replies, totalCount, currentPage, totalPage
        )
    }

}
