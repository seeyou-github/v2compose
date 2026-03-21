package io.github.v2compose.network.bean

import io.github.fruit.converter.retrofit.IBaseWrapper
import java.io.Serializable

abstract class BaseInfo : IBase, IBaseWrapper, Serializable {
    var rawResponse: String = ""
    override fun setResponse(html: String) {
        this.rawResponse = html
    }

    override fun getResponse(): String = rawResponse
}
