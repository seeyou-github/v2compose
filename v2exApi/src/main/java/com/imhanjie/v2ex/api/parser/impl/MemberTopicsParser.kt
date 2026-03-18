package com.imhanjie.v2ex.api.parser.impl

import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.MemberTopics
import com.imhanjie.v2ex.api.model.TopicItem
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup
import java.util.regex.Pattern
import kotlin.math.max

object MemberTopicsParser : ParserMatcher {

    override fun match(url: String, method: String): Boolean {
        return Pattern.compile("^${V2ex.BASE_URL}/member/\\w+/topics\\?p=\\d+\$").matcher(url).find()
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

        val box = document.selectFirst("#Main")?.selectFirst("div.box")
        val hide = box?.selectFirst("div.inner") != null
        val totalCount = document.selectFirst("div.header")?.selectFirst("strong.gray")?.text()?.toIntOrNull() ?: 0
        val topics = document.select("div.cell.item").map { eCell ->
            val eTitle = eCell.selectFirst("a.topic-link")
            val isTop = eCell.attr("style").isNotEmpty()
            val title = eTitle?.text() ?: ""
            val id = eTitle?.attr("href")?.split("/")?.getOrNull(2)?.split("#")?.getOrNull(0)?.toLongOrNull() ?: 0L

            val eTopicInfo = eCell.selectFirst("span.topic_info")

            val eNode = eTopicInfo?.selectFirst("a.node")
            val nodeTitle = eNode?.text() ?: ""
            val nodeName = eNode?.attr("href")?.split("/")?.getOrNull(2) ?: ""

            val userName = eTopicInfo?.selectFirst("strong > a")?.text() ?: ""

//            val userAvatar = eCell.selectFirst("img.avatar").attr("src")
            val latestReplyTime = eTopicInfo?.text()?.split(" • ")?.getOrNull(2) ?: ""

            val replies = eCell.selectFirst("a.count_livid")?.text()?.toLongOrNull() ?: 0L

            TopicItem(
                id,
                title,
                nodeName,
                nodeTitle,
                userName,
                "",
                latestReplyTime,
                replies,
                isTop
            )
        }

        return MemberTopics(
            topics, hide, totalCount, currentPage, totalPage
        )
    }

}
