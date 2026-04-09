package io.github.v2compose.shared.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DraftTopic(
    val title: String = "",
    val content: String = "",
    val contentFormat: ContentFormat = ContentFormat.Original,
    val node: TopicNode? = null,
) {

    companion object {

        val Empty = DraftTopic()

        fun fromJson(json: String): DraftTopic {
            return runCatching { Json.decodeFromString<DraftTopic>(json) }.getOrDefault(Empty)
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}

@Serializable
enum class ContentFormat {
    Original, Markdown
}
