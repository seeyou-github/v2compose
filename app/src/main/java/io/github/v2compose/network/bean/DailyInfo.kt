package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp
import io.github.v2compose.util.UriUtils
import io.github.v2compose.util.Utils


@Pulp
class DailyInfo : BaseInfo() {
    @Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = ""

    @Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = ""

    @Pick("h1")
    val title: String = ""

    @Pick("div.cell:contains(已连续)")
    val continuousLoginDaysText: String = ""

    @Pick(value = "div.cell input[type=button]", attr = "onclick")
    val checkInUrl: String = "" // location.href = '/mission/daily/redeem?once=84830';

    fun hadCheckedIn(): Boolean {
        return checkInUrl.isNotEmpty() && checkInUrl == "location.href = '/balance';"
    }

    fun continuousLoginDays(): String = Utils.extractDigits(continuousLoginDaysText)

    private var _userName: String? = null
    val userName: String?
        get() {
            if (_userName != null) return _userName
            if (userLink.isEmpty()) return null
            _userName = userLink.split("/").getOrNull(2)
            return _userName
        }

    private var _once: String? = null
    val once: String
        get() {
            if (_once != null) return _once!!
            var result = UriUtils.getParamValue(checkInUrl, "once")
            if (!result.isNullOrEmpty()) {
                result = result.replace("';", "")
            }
            _once = result ?: ""
            return _once!!
        }

    override fun isValid(): Boolean = checkInUrl.isNotEmpty()

    override fun toString(): String {
        return "DailyInfo(title='$title', checkInUrl='$checkInUrl', once='$once')"
    }
}
