package io.github.v2compose.util

object InetValidator {

    private val hostOrIpRegex = Regex(
        "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)+([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$"
    )

    fun isValidHostOrIp(hostOrIp: String): Boolean {
        return hostOrIpRegex.matches(hostOrIp)
    }

    fun isValidInetPort(inetPort: Int): Boolean {
        return inetPort in 0..65535
    }
}
