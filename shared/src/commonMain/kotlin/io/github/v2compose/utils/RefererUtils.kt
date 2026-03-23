package io.github.v2compose.utils

import io.github.v2compose.network.NetConstants

object RefererUtils {
    val TINY_REFER: String = NetConstants.BASE_URL + "/mission/daily"

    fun topicReferer(topicId: String?): String {
        return NetConstants.BASE_URL + "/t/" + topicId
    }

    fun userReferer(username: String?): String {
        return NetConstants.BASE_URL + "/member/" + username
    }
}
