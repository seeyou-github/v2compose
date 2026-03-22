package io.github.v2compose.shared.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ProxyInfo(
    val type: ProxyType = ProxyType.Direct,
    val address: String = "",
    val port: Int = 0,
) {
    companion object {
        val Default = ProxyInfo()

        fun fromJson(json: String): ProxyInfo {
            return runCatching { Json.decodeFromString<ProxyInfo>(json) }.getOrDefault(Default)
        }
    }

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}

@Serializable
enum class ProxyType { System, Direct, Http, Socks }
