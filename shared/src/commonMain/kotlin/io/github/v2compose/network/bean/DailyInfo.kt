package io.github.v2compose.network.bean

import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

@Slice
data class DailyInfo(
    @property:Pick(value = "[href^=/member]", attr = "href")
    val userLink: String = "",
    @property:Pick(value = "img[src*=avatar/]", attr = "src")
    val avatar: String = "",
    @property:Pick("h1")
    val title: String = "",
    @property:Pick("div.cell:contains(已连续)")
    val continuousLoginDaysText: String = "",
    @property:Pick(value = "div.cell input[type=button]", attr = "onclick")
    val checkInUrl: String = "",
) {
    val userName: String?
        get() = userLink.split("/").getOrNull(2)

    val once: String
        get() {
            val match = Regex("once=([^'&]+)").find(checkInUrl)
            return match?.groupValues?.getOrNull(1)?.replace("';", "").orEmpty()
        }

    fun hadCheckedIn(): Boolean = checkInUrl == "location.href = '/balance';"

    fun continuousLoginDays(): String = continuousLoginDaysText.replace(Regex("[^0-9]"), "")

    fun isValid(): Boolean = checkInUrl.isNotEmpty()
}
