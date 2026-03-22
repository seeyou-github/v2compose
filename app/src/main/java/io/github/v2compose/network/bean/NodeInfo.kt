package io.github.v2compose.network.bean

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName
import io.github.v2compose.util.AvatarUtils
import java.io.Serializable

/**
 * 节点详情
 * https://www.v2ex.com/api/nodes/show.json?name=qna
 */
@Stable
class NodeInfo : BaseInfo(), Serializable {
    @SerializedName("id")
    val id: Int = 0

    @SerializedName("name")
    val name: String = ""

    @SerializedName("url")
    val url: String = ""

    @SerializedName("title")
    val title: String = ""

    @SerializedName("topics")
    val topics: Int = 0

    @SerializedName("stars")
    val stars: Int = 0

    @SerializedName("header")
    val header: String = ""

    @SerializedName("created")
    val created: Long = 0

    @SerializedName("avatar_large")
    val avatar: String = ""

    override fun toString(): String {
        return "NodeInfo(id=$id, name='$name', url='$url', title='$title', topics=$topics, stars=$stars, header='$header', created=$created, avatar='$avatar')"
    }

    override fun isValid(): Boolean {
        return name.isNotEmpty()
    }
}
