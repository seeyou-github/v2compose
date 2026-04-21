package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class ReplyTopicResultInfo(
    @property:Pick(value = "input[name=once]", attr = "value")
    val once: String = "",
    @property:Pick(value = "div.problem", attr = Attrs.HTML)
    val problem: String = "",
) {
    fun isValid(): Boolean = once.isNotEmpty()
}
