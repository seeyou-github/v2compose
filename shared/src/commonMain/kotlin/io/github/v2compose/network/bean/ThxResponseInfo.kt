package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp
data class ThxResponseInfo(
    @property:Pick(value = "a[href=/balance]", attr = Attrs.HREF)
    val link: String = "",
) {
    fun isValid(): Boolean = link.isNotEmpty()
}
