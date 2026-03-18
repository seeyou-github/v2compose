package com.imhanjie.v2ex.api.parser.impl

import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.Reply
import com.imhanjie.v2ex.api.model.Topic
import com.imhanjie.v2ex.api.support.RegexPattern
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.math.max

object TopicParser : ParserMatcher {

    override fun match(url: String, method: String): Boolean {
        return url.startsWith("${V2ex.BASE_URL}/t/")
    }

    override fun parser(html: String): Any {
        val document = Jsoup.parse(html)

        val id = document.selectFirst("div.votes")?.attr("id")?.split("_")?.getOrNull(1)?.toLongOrNull() ?: 0L
        val mainBox = document.selectFirst("#Main")?.selectFirst("div.box")
        val header = mainBox?.selectFirst("div.header")
        val title = header?.selectFirst("h1")?.text() ?: ""

        var nodeName = ""
        var nodeTitle = ""
        header?.select("a")?.forEach { ae ->
            val href = ae.attr("href")
            val key = "/go/"
            if (href.isNotEmpty() && href.startsWith(key)) {
                nodeName = href.split(key).getOrNull(1) ?: ""
                nodeTitle = ae.text()
                return@forEach
            }
        }

        val userAvatar = header?.selectFirst("img.avatar")?.attr("src") ?: ""
        val userName: String
        val createTime: String
        val click: Long
        val smallGray = header?.selectFirst("small.gray")
        if (smallGray != null) {
            val parts = smallGray.text().split(" · ")
            userName = parts.getOrNull(0) ?: ""
            createTime = parts.getOrNull(1) ?: ""
            click = parts.getOrNull(2)?.split(" ")?.getOrNull(0)?.toLongOrNull() ?: 0L
        } else {
            userName = ""
            createTime = ""
            click = 0L
        }

        val richContent = (mainBox?.selectFirst("div.markdown_body")?.html() ?: mainBox?.selectFirst("div.topic_content")?.html()) ?: ""

        val currentPage = document.selectFirst("a.page_current")?.text()?.toIntOrNull() ?: 1
        var totalPage = currentPage
        document.select("a.page_normal").let {
            if (it.isNotEmpty()) {
                totalPage = max(it.last()?.text()?.toIntOrNull() ?: currentPage, currentPage)
            }
        }

        // 附言
        var subtleNo = 1
        val subtleList = document.select("div.subtle").map {
            val subtleCreateTime = it.selectFirst("span.fade")?.text()?.split(" · ")?.getOrNull(1) ?: ""
            val subtleRichContent = it.selectFirst("div.topic_content")?.html() ?: ""
            Topic.Subtle(
                subtleNo++,
                subtleCreateTime,
                subtleRichContent
            )
        }

        var once = ""
        val onceMatcher = RegexPattern.PAGE_ONCE.matcher(html)
        if (onceMatcher.find()) {
            once = onceMatcher.group().split("=")[1]
        }

        val rightBarBox = document.selectFirst("#Rightbar")?.selectFirst("div.box")
        val rightCell = rightBarBox?.selectFirst("div.cell")
        val isMyTopic = userName != "" && userName == rightCell?.selectFirst("span.bigger")?.text()

        var favoriteParam = ""
        var isFavorite = false
        document.selectFirst("#Main")?.selectFirst("div.topic_buttons")?.selectFirst("a.tb")?.let {
            val href = it.attr("href")
            favoriteParam = href.split("=").getOrNull(1) ?: ""
            isFavorite = it.text() == "取消收藏"
        }

        val canAppend = header?.select("a")?.firstOrNull { it.text() == "APPEND" } != null

        val replies: List<Reply> = parserReplies(document)
        return Topic(
            id,
            title,
            nodeName,
            nodeTitle,
            userName,
            userAvatar,
            createTime,
            click,
            richContent,
            subtleList,
            replies,
            currentPage,
            totalPage,
            once,
            isMyTopic,
            favoriteParam,
            isFavorite,
            canAppend
        )
    }

    private fun parserReplies(document: Document): List<Reply> {
        return document.select("#Main").select("div.cell")
            .filter { eCell ->
                val attrId = eCell.attr("id")
                attrId.isNotEmpty() && attrId.startsWith("r_")
            }
            .map { eCell ->
                val replyId = eCell.attr("id").split("_").getOrNull(1)?.toLongOrNull() ?: 0L
                val userAvatar = eCell.selectFirst("img.avatar")?.attr("src") ?: ""
                val userName = eCell.selectFirst("a.dark")?.text() ?: ""
                val content = eCell.selectFirst("div.reply_content")?.html() ?: ""
                val time = eCell.selectFirst("span.ago")?.text() ?: ""
                val thankCount = eCell.selectFirst("span.small.fade")?.text()?.toLongOrNull() ?: 0L
                val thanked = (eCell.selectFirst("div.thank_area")?.selectFirst("div.thanked")) != null
                val no = eCell.selectFirst("span.no")?.text()?.toLongOrNull() ?: 0L
                Reply(
                    replyId, userAvatar, userName, content, time, thankCount, thanked, no
                )
            }
    }

}
