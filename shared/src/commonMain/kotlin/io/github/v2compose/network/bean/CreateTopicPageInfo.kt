package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Slice

@Slice("div#Wrapper")
data class CreateTopicPageInfo(
    @property:Pick(value = "input[name=once]", attr = "value")
    val once: String = "",
    @property:Pick("div.problem")
    val problem: Problem? = null,
) {
    fun isValid(): Boolean = once.isNotEmpty()

    @Slice
    data class Problem(
        @property:Pick(attr = Attrs.HTML)
        val html: String = "",
        @property:Pick(attr = Attrs.OWN_TEXT)
        val title: String = "",
        @property:Pick("ul li")
        val tips: List<String> = emptyList(),
    ) {
        fun isEmpty(): Boolean = tips.isEmpty() && title.isEmpty()
    }
}
