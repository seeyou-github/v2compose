package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp("div#Wrapper")
data class AppendTopicPageInfo(
    @property:Pick(value = "input[name=once]", attr = "value")
    val once: String = "",
    @property:Pick("div.inner ul li")
    val tips: List<Tip> = emptyList(),
    @property:Pick("div.problem")
    val problem: Problem? = null,
) {
    fun isValid(): Boolean = once.isNotEmpty() && tips.size > 1

    @Pulp
    data class Tip(
        @property:Pick
        val text: String = ""
    )

    @Pulp
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
