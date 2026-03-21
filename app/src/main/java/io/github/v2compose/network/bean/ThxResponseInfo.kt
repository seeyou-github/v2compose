package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

/**
 * Created by ghui on 22/06/2017.
 */
@Pulp
class ThxResponseInfo : BaseInfo() {
    @Pick(value = "a[href=/balance]", attr = Attrs.HREF)
    private val link: String = ""

    override fun isValid(): Boolean {
        return link.isNotEmpty()
    }
}
