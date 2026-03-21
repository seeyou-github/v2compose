package io.github.v2compose.network.bean

import com.google.gson.annotations.SerializedName
import io.github.v2compose.util.Check
import org.json.JSONObject
import java.io.Serializable

/**
 * Created by ghui on 03/05/2017.
 */
class UserInfo : BaseInfo(), Serializable {
    @SerializedName("status")
    var status: String = ""

    @SerializedName("id")
    var id: String = ""

    @SerializedName("username")
    var userName: String = ""

    @SerializedName("website")
    var website: String = ""

    @SerializedName("twitter")
    var twitter: String = ""

    @SerializedName("psn")
    var psn: String = ""

    @SerializedName("github")
    var github: String = ""

    @SerializedName("btc")
    var btc: String = ""

    @SerializedName("location")
    var location: String = ""

    @SerializedName("tagline")
    var tagline: String = ""

    @SerializedName("bio")
    var bio: String = ""

    @SerializedName("avatar_large")
    var avatar: String = ""

    @SerializedName("avatar_xlarge")
    var avatarX: String = ""

    @SerializedName("avatar_xxlarge")
    var avatarXx: String = ""

    @SerializedName("avatar_xxxlarge")
    var avatarXxx: String = ""

    @SerializedName("created")
    var created: String = ""

    companion object {
        fun build(userName: String, avatar: String): UserInfo {
            val userInfo = UserInfo()
            userInfo.userName = userName
            userInfo.avatar = avatar
            return userInfo
        }
    }

    val avatarUrl: String
        get() {
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

    val largestAvatar: String
        get() {
            if (avatarXxx.isNotEmpty()) return avatarXxx
            if (avatarXx.isNotEmpty()) return avatarXx
            if (avatarX.isNotEmpty()) return avatarX
            return avatarUrl
        }

    override fun isValid(): Boolean {
        return id.isNotEmpty()
    }

    val userBasicInfo: String?
        get() {
            return try {
                JSONObject().apply {
                    put("id", id)
                    put("name", userName)
                }.toString()
            } catch (e: Exception) {
                null
            }
        }
}
