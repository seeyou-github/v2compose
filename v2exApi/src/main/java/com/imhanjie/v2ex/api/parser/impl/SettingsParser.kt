package com.imhanjie.v2ex.api.parser.impl

import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.MyUserInfo
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup

object SettingsParser : ParserMatcher {

    override fun match(url: String, method: String): Boolean {
        return url == "${V2ex.BASE_URL}/settings/privacy"
    }

    override fun parser(html: String): Any {
        val document = Jsoup.parse(html)
        val eCell = document.selectFirst("#Rightbar")
            ?.selectFirst("div.box")
            ?.selectFirst("div.cell")
        val avatar = eCell?.selectFirst("img.avatar")?.attr("src") ?: ""
        val userName = eCell?.selectFirst("span.bigger")?.text() ?: ""
        val eCounts = eCell?.select("table")?.getOrNull(1)?.select("span.bigger") ?: emptyList()

        val eMoney = document.selectFirst("#money")?.selectFirst("a")
        val moneyArray = eMoney?.text()?.split(" ") ?: emptyList()
        val moneyGold = moneyArray.getOrNull(0)?.toLongOrNull() ?: 0L
        val moneySilver = moneyArray.getOrNull(1)?.toLongOrNull() ?: 0L
        val moneyBronze = moneyArray.getOrNull(2)?.toLongOrNull() ?: 0L

        return MyUserInfo(
            userName,
            avatar,
            eCounts.getOrNull(0)?.text()?.toLongOrNull() ?: 0L,
            eCounts.getOrNull(1)?.text()?.toLongOrNull() ?: 0L,
            eCounts.getOrNull(2)?.text()?.toLongOrNull() ?: 0L,
            moneyGold,
            moneySilver,
            moneyBronze
        )
    }

}
