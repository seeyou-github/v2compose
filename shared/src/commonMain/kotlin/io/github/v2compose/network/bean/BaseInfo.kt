package io.github.v2compose.network.bean

import io.github.fruit.IBaseWrapper

abstract class BaseInfo : IBase, IBaseWrapper {
    var rawResponse: String = ""
    override fun setResponse(html: String) {
        this.rawResponse = html
    }

    override fun getResponse(): String = rawResponse
}
