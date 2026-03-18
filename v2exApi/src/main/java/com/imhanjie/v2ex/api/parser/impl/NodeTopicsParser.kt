package com.imhanjie.v2ex.api.parser.impl

import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.Node
import com.imhanjie.v2ex.api.model.TopicItem
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup
import kotlin.math.max

object NodeTopicsParser : ParserMatcher {

    override fun match(url: String, method: String): Boolean {
        return url.startsWith("${V2ex.BASE_URL}/go/")
    }

    override fun parser(html: String): Any {
        val document = Jsoup.parse(html)

        val nodeTitle = document.selectFirst("title")?.text()?.split(" › ")?.getOrNull(1) ?: ""

        val eNodeInfo = document.selectFirst("div.node_info")
        val nodeDesc = eNodeInfo?.selectFirst("span.f12")?.text() ?: ""
        val topicTotalCount = eNodeInfo?.selectFirst("div.fr.f12")?.selectFirst("strong")?.text()?.toLongOrNull() ?: 0L
        val eA = eNodeInfo?.selectFirst("div.fr.f12")?.selectFirst("a")
        var isFavorite = false
        var once = ""
        var nodeId = 0L
        eA?.let {
            isFavorite = it.text() == "取消收藏"
            val href = it.attr("href")
            once = href.split("=").last()
            nodeId = href.split("?").getOrNull(0)?.split("/")?.last()?.toLongOrNull() ?: 0L
        }
        val nodeAvatar = document.selectFirst("div.node_avatar")?.selectFirst("img")?.attr("src") ?: ""

        val searchStr = "var nodeName = \""
        val startIndex = html.indexOf(searchStr)
        val nodeName = if (startIndex != -1) {
            val start = startIndex + searchStr.length
            val endIndex = html.indexOf("\"", start)
            if (endIndex != -1) html.substring(start, endIndex) else ""
        } else {
            ""
        }

        val topics = document.select("#TopicsNode").select("div.cell").map { eCell ->
            val eTitle = eCell.selectFirst("a.topic-link")
            val title = eTitle?.text() ?: ""
            val id = eTitle?.attr("href")?.split("/")?.getOrNull(2)?.split("#")?.getOrNull(0)?.toLongOrNull() ?: 0L

            val eTopicInfo = eCell.selectFirst("span.topic_info")

            val userName = eTopicInfo?.selectFirst("strong > a")?.text() ?: ""

            val userAvatar = eCell.selectFirst("img.avatar")?.attr("src") ?: ""
            val latestReplyTime = eTopicInfo?.text()?.split(" • ")?.getOrNull(1) ?: ""

            val replies = eCell.selectFirst("a.count_livid")?.text()?.toLongOrNull() ?: 0L

            TopicItem(
                id,
                title,
                nodeName,
                nodeTitle,
                userName,
                userAvatar,
                latestReplyTime,
                replies,
                isTop = false
            )
        }

        val currentPage = document.selectFirst("a.page_current")?.text()?.toIntOrNull() ?: 1
        var totalPage = currentPage
        document.select("a.page_normal").let {
            if (it.isNotEmpty()) {
                totalPage = max(it.last()?.text()?.toIntOrNull() ?: currentPage, currentPage)
            }
        }
        return Node(
            nodeId,
            nodeTitle,
            nodeName,
            nodeAvatar,
            nodeDesc,
            topicTotalCount,
            isFavorite,
            topics,
            currentPage,
            totalPage,
            once
        )
    }

}
