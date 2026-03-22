package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp
class DailyInfo : BaseInfo() {
    @Pick(value = "[href^=/member]", attr = "href")
    var userLink: String = ""

    @Pick(value = "img[src*=avatar/]", attr = "src")
    var avatar: String = ""

    @Pick("h1")
    var title: String = ""

    @Pick("div.cell:contains(已连续)")
    var continuousLoginDaysText: String = ""

    @Pick(value = "div.cell input[type=button]", attr = "onclick")
    var checkInUrl: String = "" // location.href = '/mission/daily/redeem?once=84830';

    fun hadCheckedIn(): Boolean {
        return checkInUrl.isNotEmpty() && checkInUrl == "location.href = '/balance';"
    }

    fun continuousLoginDays(): String {
        if (continuousLoginDaysText.isEmpty()) return ""
        return continuousLoginDaysText.replace(Regex("[^0-9]"), "")
    }

    var _userName: String? = null
    val userName: String?
        get() {
            if (_userName != null) return _userName
            if (userLink.isEmpty()) return null
            _userName = userLink.split("/").getOrNull(2)
            return _userName
        }

    var _once: String? = null
    val once: String
        get() {
            if (_once != null) return _once!!
            // Extract once parameter from checkInUrl like: location.href = '/mission/daily/redeem?once=84830';
            var regex = Regex("once=([^'&]+)")
            var match = regex.find(checkInUrl)
            var result = match?.groupValues?.getOrNull(1)
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
