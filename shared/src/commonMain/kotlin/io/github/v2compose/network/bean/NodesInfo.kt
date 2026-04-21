package io.github.v2compose.network.bean

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * https://v2ex.com/api/nodes/s2.json
 */
@Serializable(with = NodesInfoSerializer::class)
data class NodesInfo(
    val items: List<Node> = emptyList()
) {
    fun isValid(): Boolean = items.isEmpty() || items[0].id.isNotEmpty()

    @Serializable
    data class Node(
        val text: String = "",
        val topics: Int = 0,
        val id: String = "",
        val isHot: Boolean = false
    ) : Comparable<Node> {
        override fun compareTo(other: Node): Int {
            val flag1 = if (isHot) 0 else 1
            val flag2 = if (other.isHot) 0 else 1
            return flag1 - flag2
        }
    }
}

object NodesInfoSerializer : KSerializer<NodesInfo> {
    private val delegate = ListSerializer(NodesInfo.Node.serializer())

    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): NodesInfo {
        return NodesInfo(items = delegate.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: NodesInfo) {
        delegate.serialize(encoder, value.items)
    }
}
