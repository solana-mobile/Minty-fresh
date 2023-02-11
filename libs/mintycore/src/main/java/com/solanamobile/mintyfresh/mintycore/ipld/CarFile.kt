package com.solanamobile.mintyfresh.mintycore.ipld

class CarFile(val rootCid: CID) {

    val headerBlock = buildHeader(listOf(rootCid))
    val dataBlocks = mutableMapOf<CID, ByteArray>()

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

    fun add(cid: CID, data: ByteArray): CarFile {
        dataBlocks.put(cid, data)
        return this
    }

    // https://ipld.io/specs/transport/car/carv1/#header
    private fun buildHeader(rootCids: List<CID>) =
        DagCborEncoder()
            .encodeMap(mapOf(
                "roots" to rootCids,
                "version" to 1
            ))
}