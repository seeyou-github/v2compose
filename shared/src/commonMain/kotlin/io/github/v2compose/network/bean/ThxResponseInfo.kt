package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp


@Pulp
class ThxResponseInfo : BaseInfo() {
    @Pick(value = "a[href=/balance]", attr = Attrs.HREF)
    var link: String = ""

    override fun isValid(): Boolean {
        return link.isNotEmpty()
    }
}
