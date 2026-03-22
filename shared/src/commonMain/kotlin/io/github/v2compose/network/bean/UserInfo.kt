package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserInfo : BaseInfo() {
    @SerialName("status")
    var status: String = ""

    @SerialName("id")
    var id: String = ""

    @SerialName("username")
    var userName: String = ""

    @SerialName("website")
    var website: String = ""

    @SerialName("twitter")
    var twitter: String = ""

    @SerialName("psn")
    var psn: String = ""

    @SerialName("github")
    var github: String = ""

    @SerialName("btc")
    var btc: String = ""

    @SerialName("location")
    var location: String = ""

    @SerialName("tagline")
    var tagline: String = ""

    @SerialName("bio")
    var bio: String = ""

    @SerialName("avatar_large")
    var avatar: String = ""

    @SerialName("avatar_xlarge")
    var avatarX: String = ""

    @SerialName("avatar_xxlarge")
    var avatarXx: String = ""

    @SerialName("avatar_xxxlarge")
    var avatarXxx: String = ""

    @SerialName("created")
    var created: String = ""

    companion object {
        fun build(userName: String, avatar: String): UserInfo {
            val userInfo = UserInfo()
            userInfo.userName = userName
            userInfo.avatar = avatar
            return userInfo
        }
    }

    fun avatarUrl(): String {
        var result = if (!avatar.startsWith("http")) "https:$avatar" else avatar
        if (!result.contains("large.png")) {
            if (result.contains("mini.png")) {
                result = result.replace("mini.png", "large.png")
            } else if (result.contains("normal.png")) {
                result = result.replace("normal.png", "large.png")
            }
        }
        return result
    }

    fun largestAvatar(): String {
        if (avatarXxx.isNotEmpty()) return avatarXxx
        if (avatarXx.isNotEmpty()) return avatarXx
        if (avatarX.isNotEmpty()) return avatarX
        return avatarUrl()
    }

    override fun isValid(): Boolean {
        return id.isNotEmpty()
    }

    fun userBasicInfo(): String? {
        return try {
            """{"id":"$id","name":"$userName"}"""
        } catch (e: Exception) {
            null
        }
    }
}
