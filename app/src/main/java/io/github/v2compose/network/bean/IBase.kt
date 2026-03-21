package io.github.v2compose.network.bean

import io.github.fruit.converter.retrofit.IBaseWrapper

interface IBase : IBaseWrapper {
    fun isValid(): Boolean

}
