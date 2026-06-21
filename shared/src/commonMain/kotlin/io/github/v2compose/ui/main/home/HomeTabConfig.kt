package io.github.v2compose.ui.main.home

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

@Serializable
data class HomeTabConfig(
    /** Stable id used for pager keys and persistence */
    val id: String,
    /** Display name in tab row */
    val name: String,
    /** Built-in News tab value (all/hot/tech...) when not a node tab */
    val newsTabValue: String? = null,
    /** Node name when this tab represents a node */
    val nodeName: String? = null,
    /** Whether this tab is visible */
    val enabled: Boolean = true,
) {
    fun isNodeTab(): Boolean = !nodeName.isNullOrBlank()

    companion object {
        // Keep Json local to avoid leaking configuration across modules.
        private val Codec = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun defaultTabs(): List<HomeTabConfig> = listOf(
            HomeTabConfig(id = "news:all", name = "全部", newsTabValue = "all"),
            HomeTabConfig(id = "news:hot", name = "最热", newsTabValue = "hot"),
            HomeTabConfig(id = "news:recent", name = "最近", newsTabValue = "recent"),
            HomeTabConfig(id = "news:tech", name = "技术", newsTabValue = "tech"),
            HomeTabConfig(id = "news:creative", name = "创意", newsTabValue = "creative"),
            HomeTabConfig(id = "news:play", name = "好玩", newsTabValue = "play"),
            HomeTabConfig(id = "news:apple", name = "Apple", newsTabValue = "apple"),
            HomeTabConfig(id = "news:jobs", name = "酷工作", newsTabValue = "jobs"),
            HomeTabConfig(id = "news:deals", name = "交易", newsTabValue = "deals"),
            HomeTabConfig(id = "news:city", name = "城市", newsTabValue = "city"),
            HomeTabConfig(id = "news:qna", name = "问与答", newsTabValue = "qna"),
            HomeTabConfig(id = "news:r2", name = "R2", newsTabValue = "r2"),
            HomeTabConfig(id = "news:nodes", name = "节点", newsTabValue = "nodes"),
            HomeTabConfig(id = "news:members", name = "关注", newsTabValue = "members"),
        )

        fun decodeList(json: String): List<HomeTabConfig> {
            if (json.isBlank()) return emptyList()
            return runCatching {
                Codec.decodeFromString(ListSerializer(HomeTabConfig.serializer()), json)
            }.getOrDefault(emptyList())
        }

        fun encodeList(list: List<HomeTabConfig>): String {
            return Codec.encodeToString(ListSerializer(HomeTabConfig.serializer()), list)
        }
    }
}

fun List<HomeTabConfig>.effectiveHomeTabs(): List<HomeTabConfig> {
    val source = if (isEmpty()) HomeTabConfig.defaultTabs() else this
    return source.filter { it.enabled }
}

fun List<HomeTabConfig>.normalized(): List<HomeTabConfig> {
    // Drop invalid entries; keep order.
    return filter { it.id.isNotBlank() && it.name.isNotBlank() && (it.newsTabValue != null || it.nodeName != null) }
}
