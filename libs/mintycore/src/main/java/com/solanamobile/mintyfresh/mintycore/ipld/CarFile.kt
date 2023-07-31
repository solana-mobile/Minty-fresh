package com.solanamobile.mintyfresh.mintycore.ipld

import androidx.annotation.Size
import com.solanamobile.mintyfresh.mintycore.util.Varint
import com.solanamobile.mintyfresh.mintycore.util.asVarint

open class CarFile(val rootCid: CID, val dataBlocks: Map<CID, ByteArray>) {

    private val headerBlock = buildHeader(listOf(rootCid))

    /*
     * https://ipld.io/specs/transport/car/carv1/#format-description
     * |--------- Header --------| |---------------------- Data -----------------------|
     * [ varint | DAG-CBOR block ] [ varint | CID | block ] [ varint | CID | block ] …
     */
    fun serialize() =
        // |------------------- Header ------------------|
        // [  varint (header size)  | DAG-CBOR block ]
        headerBlock.size.asVarint() +   headerBlock  +
                dataBlocks.flatMap { (cid, data) ->
                    //     |----------------------------- Block -------------------------------|
                    //     [        varint block size (cid + data)         |    CID    | block ]
                    listOf( Varint.encode(cid.bytes.size + data.size) + cid.bytes + data )
                }.reduce{ a, d -> a + d }

    open class Builder {

        private var rootCid: CID? = null
        private val dataBlocks = mutableMapOf<CID, ByteArray>()

        fun add(cid: CID, @Size(max = MAX_BLOCK_SIZE.toLong()) data: ByteArray,
                isRoot: Boolean = false): Builder {
            dataBlocks[cid] = data
            if (isRoot) setRoot(cid)
            return this
        }

        fun add(blocks: Map<CID, ByteArray>): Builder {
            blocks.forEach { (cid, data) -> add(cid, data) }
            return this
        }

        fun addRoot(rootCid: CID, @Size(max = MAX_BLOCK_SIZE.toLong()) data: ByteArray) =
            add(rootCid, data, true)

        fun setRoot(rootCid: CID): Builder {
            this.rootCid = rootCid
            return this
        }

        open fun build(): CarFile {
            check(rootCid != null) { "Invalid Root: no Root CID was provided" }
            check(dataBlocks.containsKey(rootCid)) { "Invalid Root: Root CID not found" }

            return CarFile(rootCid!!, dataBlocks)
        }
    }

    // https://ipld.io/specs/transport/car/carv1/#header
    private fun buildHeader(rootCids: List<CID>) =
        DagCborEncoder()
            .encodeMap(mapOf(
                "roots" to rootCids,
                "version" to 1
            ))
    companion object {
        const val MAX_BLOCK_SIZE = 1 shl 20 // 1MB
    }
}