package com.solanamobile.mintyfresh.mintycore.ipld

/*
 * Minimum viable implementation of IPLD DAG_PB encoding spec
 * see here: https://github.com/ipld/specs/blob/master/block-layer/codecs/dag-pb.md
 * https://github.com/ipld/js-dag-pb/blob/master/src/pb-encode.js
 */

/**
 * Implementation of a IPLD Protobuf Node according to the DAG-PB logical format
 * see here: https://ipld.io/specs/codecs/dag-pb/spec/#logical-format
 */
open class PBNode(val data: ByteArray, val links: List<PBLink>?)

/**
 * Implementation of a IPLD Protobuf Link according to the DAG-PB logical format
 * see here: https://ipld.io/specs/codecs/dag-pb/spec/#logical-format
 */
data class PBLink(val name: String?, val size: Int, val cid: CID)

/**
 * Encodes an IPLD Protobuf Node according to the DAG-PB standard
 * see here: https://github.com/ipld/js-dag-pb/blob/master/src/pb-encode.js
 */
fun PBNode.encode(): ByteArray {

    val linkBlocks = links?.map {
        val bytes = it.encode()
        byteArrayOf(0x12) + Varint.encode(bytes.size) + bytes
    }

    val dataBlock = if (data.isNotEmpty())
        byteArrayOf(0xa) + Varint.encode(data.size) + data
    else byteArrayOf()

    return (linkBlocks?.reduce { acc, bytes -> acc + bytes } ?: byteArrayOf()) + dataBlock
}

/**
 * Encodes an IPLD Protobuf Link according to the DAG-PB standard
 * see here: https://github.com/ipld/js-dag-pb/blob/master/src/pb-encode.js
 */
fun PBLink.encode() =
    byteArrayOf(0x0a) + Varint.encode(cid.bytes.size) + cid.bytes +                     // link cid
            (name?.let {
                byteArrayOf(0x12) + Varint.encode(name.length) + name.encodeToByteArray()
            } ?: byteArrayOf()) +                                                       // link name
            byteArrayOf(0x18) + Varint.encode(size)                                     // link size