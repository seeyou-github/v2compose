package com.imhanjie.v2ex.api.parser.impl

import android.annotation.SuppressLint
import com.imhanjie.v2ex.api.ParserMatcher
import com.imhanjie.v2ex.api.model.SignInInfo
import com.imhanjie.v2ex.api.support.V2ex
import org.jsoup.Jsoup

object SignInInfoParser : ParserMatcher {

    @SuppressLint("DefaultLocale")
    override fun match(url: String, method: String): Boolean {
        return url == "${V2ex.BASE_URL}/signin" && method.uppercase() == "GET"
    }

    override fun parser(html: String): Any {
        val document = Jsoup.parse(html)
        val eTrs = document.selectFirst("#Main")?.selectFirst("table")?.select("tr") ?: emptyList()
        val keyUserName = eTrs.getOrNull(0)?.selectFirst("input")?.attr("name") ?: ""
        val keyPassword = eTrs.getOrNull(1)?.selectFirst("input")?.attr("name") ?: ""
        val keyVerCode = eTrs.getOrNull(2)?.selectFirst("input")?.attr("name") ?: ""
        val verUrlOnce = eTrs.getOrNull(2)?.selectFirst("div")
            ?.attr("style")
            ?.split(";")?.getOrNull(0)
            ?.split("'")?.getOrNull(1)
            ?.split("=")?.getOrNull(1) ?: ""
        return SignInInfo(
            keyUserName,
            keyPassword,
            keyVerCode,
            verUrlOnce
        )
    }

}
