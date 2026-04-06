package io.github.v2compose.network.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String?,
    @SerialName("body")
    val body: String?,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String,
) {
    companion object {
        val Empty = Release(0, "", "", "", "")

        fun fromMap(map: Map<String, Any?>): Release {
            return Release(
                id = map["id"] as Int,
                name = map["name"] as String?,
                body = map["body"] as String?,
                tagName = map["tag_name"] as String,
                htmlUrl = map["html_url"] as String,
            )
        }
    }

    fun isValid(): Boolean {
        return id > 0 && tagName.isNotEmpty() && htmlUrl.isNotEmpty()
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "body" to body,
            "tag_name" to tagName,
            "html_url" to htmlUrl,
        )
    }
}

@Serializable
data class ReleaseAsset(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("size")
    val size: Int,
    @SerialName("download_count")
    val downloadCount: Int,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
)
