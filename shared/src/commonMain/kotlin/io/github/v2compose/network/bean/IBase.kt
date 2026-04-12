package io.github.v2compose.network.bean

interface IBase {
    fun isValid(): Boolean

    fun setResponse(html: String)

    fun getResponse(): String
}
