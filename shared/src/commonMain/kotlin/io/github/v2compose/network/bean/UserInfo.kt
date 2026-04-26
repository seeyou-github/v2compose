package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("status")
    val status: String = "",
    @SerialName("id")
    val id: Int = 0,
    @SerialName("username")
    val userName: String = "",
    @SerialName("website")
    val website: String? = null,
    @SerialName("twitter")
    val twitter: String? = null,
    @SerialName("psn")
    val psn: String? = null,
    @SerialName("github")
    val github: String? = null,
    @SerialName("btc")
    val btc: String? = null,
    @SerialName("location")
    val location: String? = null,
    @SerialName("tagline")
    val tagline: String = "",
    @SerialName("bio")
    val bio: String = "",
    @SerialName("avatar_large")
    val avatar: String = "",
    @SerialName("avatar_xlarge")
    val avatarX: String = "",
    @SerialName("avatar_xxlarge")
    val avatarXx: String = "",
    @SerialName("avatar_xxxlarge")
    val avatarXxx: String = "",
    @SerialName("created")
    val created: Long = 0,
) {
    fun avatarUrl(): String {
        var result = if (!avatar.startsWith("http")) "https:$avatar" else avatar
        if (!result.contains("large.png")) {
            result = when {
                result.contains("mini.png") -> result.replace("mini.png", "large.png")
                result.contains("normal.png") -> result.replace("normal.png", "large.png")
                else -> result
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

    fun isValid(): Boolean = id > 0

    fun userBasicInfo(): String? {
        return try {
            """{"id":"$id","name":"$userName"}"""
        } catch (e: Exception) {
            null
        }
    }
}
