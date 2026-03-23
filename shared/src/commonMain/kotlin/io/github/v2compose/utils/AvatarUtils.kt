package io.github.v2compose.utils

import io.github.v2compose.network.NetConstants

object AvatarUtils {
    fun adjustAvatar(avatar: String?): String? {
        var newAvatar = avatar
        if (newAvatar.isNullOrBlank()) return null
        //1.
        if (!newAvatar.startsWith(NetConstants.HTTPS_SCHEME) && !newAvatar.startsWith(NetConstants.HTTP_SCHEME)) {
            if (newAvatar.startsWith("//")) {
                newAvatar = NetConstants.HTTPS_SCHEME + newAvatar
            } else if (newAvatar.startsWith("/")) {
                newAvatar = NetConstants.BASE_URL + newAvatar
            }
        }

        //2.
        if (newAvatar.contains("_normal.png")) {
            newAvatar = newAvatar.replace("_normal.png", "_large.png")
        } else if (newAvatar.contains("_mini.png")) {
            newAvatar = newAvatar.replace("_mini.png", "_large.png")
        }

        if (newAvatar.contains("_xxlarge.png")) {
            newAvatar = newAvatar.replace("_xxlarge.png", "_large.png")
        }

        //3. del param
//        if (avatar.contains("?")) {
//            avatar = avatar.substring(0, avatar.indexOf("?"));
//        }
        return newAvatar
    }
}
