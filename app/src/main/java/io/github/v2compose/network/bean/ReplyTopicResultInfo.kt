package io.github.v2compose.network.bean

import io.github.fruit.annotations.Attrs
import io.github.fruit.annotations.Pick
import io.github.fruit.annotations.Pulp

@Pulp(value = "div#Wrapper")
class ReplyTopicResultInfo : BaseInfo() {
    @Pick(value = "input[name=once]", attr = "value")
    var once: String = ""

    @Pick(value = "div.problem", attr = Attrs.HTML)
    var problem: String = ""

    override fun isValid(): Boolean {
        return once.isNotEmpty()
    }

    override fun toString(): String {
        return "ReplyTopicResultInfo(once='$once', problem='$problem')"
    }
}
