package com.solanamobile.mintyfresh.mintycore.ipld

import kotlin.experimental.or

// TODO: test with multiple root nodes
class CarWriter(val rootCids: List<CID>) {

    constructor(rootCID: CID): this(listOf(rootCID))

    val headerBlock = buildHeader(rootCids)
    val dataBlocks = mutableMapOf<CID, ByteArray>()

    /*
     * https://ipld.io/specs/transport/car/carv1/#format-description
     * |--------- Header --------| |---------------------- Data -----------------------|
     * [ varint | DAG-CBOR block ] [ varint | CID | block ] [ varint | CID | block ] …
     */
    fun build() =
        // |------------------- Header ------------------|
        // [    varint (header size)    | DAG-CBOR block ]
        Varint.encode(headerBlock.size) +   headerBlock  +
                dataBlocks.flatMap { (cid, data) ->
                    //     |----------------------------- Block -------------------------------|
                    //     [        varint block size (cid + data)         |    CID    | block ]
                    listOf( Varint.encode(cid.bytes.size + data.size) + cid.bytes + data )
                }.reduce{ a, d -> a + d }

    fun add(cid: CID, data: ByteArray) : CarWriter {
        // TODO: test will multiple root nodes
        dataBlocks.put(cid, data)
        return this
    }

    private fun buildHeader(rootCids: List<CID>) =
        encodeMapStart(2) + encodeRootsElement(rootCids.map { it.bytes }) + encodeVersionElement()

    private fun encodeMapStart(mapSize: Int): ByteArray {
        val CBOR_MAP_HEADER: Int = 0b101_00000
        return byteArrayOf(((CBOR_MAP_HEADER or mapSize) and 0xFF).toByte())
    }

    private fun encodeString(string: String): ByteArray {
        val CBOR_STRING_HEADER: Byte = 0b011_00000
        if (string.length < 24)
            return byteArrayOf(CBOR_STRING_HEADER or string.length.toByte()) + string.encodeToByteArray()
        else throw IllegalArgumentException("Strings longer than 23 characters are not currently supported")
    }

    private fun encodeCidArray(cids: List<ByteArray>): ByteArray {
        val CBOR_ARRAY_HEADER: Int = 0b100_00000
        if (cids.size < 24)
            return byteArrayOf(((CBOR_ARRAY_HEADER or cids.size) and 0xFF).toByte()) +
                    cids.fold(byteArrayOf()) { acc, bytes ->
                        acc + byteArrayOf(-40, 42, 88, 37, 0) + bytes // TODO: document magic numbers here
                    }
        else throw IllegalArgumentException("Lists containing more than 23 items are not currently supported")
    }

    private fun encodeInt(value: Int): ByteArray = Varint.encode(value)
    private fun encodeRootsElement(cids: List<ByteArray>) = encodeString("roots") + encodeCidArray(cids)
    private fun encodeVersionElement(version: Int = 1) = encodeString("version") + encodeInt(version)
}