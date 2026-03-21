package io.github.v2compose.network.bean

import io.github.v2compose.util.UriUtils
import io.github.v2compose.util.Utils
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * Created by ghui on 07/08/2017.
 */
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

    val continuousLoginDays: String
        get() = Utils.extractDigits(continuousLoginDaysText)

    val userName: String?
        get() {
            if (userLink.isEmpty()) return null
            return userLink.split("/").getOrNull(2)
        }

    val largeAvatar: String?
        get() {
            if (avatar.isEmpty()) return null
            return avatar.replace("normal.png", "large.png")
        }

    val once: String
        get() {
            var result = UriUtils.getParamValue(checkInUrl, "once")
            if (!result.isNullOrEmpty()) {
                result = result.replace("';", "")
            }
            return result ?: ""
        }

    override fun isValid(): Boolean {
        return checkInUrl.isNotEmpty()
    }

    override fun toString(): String {
        return "DailyInfo(title='$title', continuousLoginDays='$continuousLoginDays', checkInUrl='$checkInUrl', once='$once')"
    }
}
