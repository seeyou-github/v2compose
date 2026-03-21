package io.github.v2compose.network.bean

import android.os.Parcelable
import io.github.v2compose.util.Check
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * https://v2ex.com/api/nodes/s2.json
 */
class NodesInfo : ArrayList<NodesInfo.Node>(), IBase, Serializable {
    private var responseBody: String = ""

    override fun isValid(): Boolean {
        if (isEmpty()) return true
        return this[0].id.isNotEmpty()
    }

    override fun getResponse(): String = responseBody

    override fun setResponse(response: String) {
        responseBody = response
    }

    @Parcelize
    class Node(
        var text: String = "",
        var topics: Int = 0,
        var id: String = "",
        var isHot: Boolean = false
    ) : Serializable, Parcelable, Comparable<Node> {

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
