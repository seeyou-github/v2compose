package io.github.v2compose.network.bean

abstract class BaseInfo : IBase {
    var rawResponse: String = ""
    override fun setResponse(html: String) {
        this.rawResponse = html
    }

    override fun getResponse(): String = rawResponse
}
