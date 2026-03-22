package io.github.v2compose.network.bean

import kotlinx.serialization.Serializable

/**
 * https://v2ex.com/api/nodes/s2.json
 */
class NodesInfo(private val delegate: MutableList<Node> = mutableListOf()) : MutableList<NodesInfo.Node> by delegate, IBase {
    private var responseBody: String = ""

    override fun isValid(): Boolean {
        if (isEmpty()) return true
        return this[0].id.isNotEmpty()
    }

    override fun getResponse(): String = responseBody

    override fun setResponse(html: String) {
        responseBody = html
    }

    @Serializable
    data class Node(
        var text: String = "",
        var topics: Int = 0,
        var id: String = "",
        var isHot: Boolean = false
    ) : Comparable<Node> {

        override fun toString(): String {
            return "Node(text='$text', topics=$topics, id='$id', isHot=$isHot)"
        }

        override fun compareTo(other: Node): Int {
            val flag1 = if (this.isHot) 0 else 1
            val flag2 = if (other.isHot) 0 else 1
            return flag1 - flag2
        }
    }
}
