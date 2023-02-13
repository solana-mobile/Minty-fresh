package com.solanamobile.mintyfresh.mintycore.ipld

/**
 * A partial implementation of a CBOR encoder that supports a the IPLD DAG_CBOR encoding spec
 *
 * Normally an encoder of this type would extend from kotlinx.serialization.AbstractEncoder and
 * provide an associated BinaryFormat so it can be used with any serializable object. Because we
 * are only using this encoder to write the CAR header block, we have provided only the minimum
 * implementation needed to accomplish the serialization of the CAR file header.
 *
 * Note: The existing kotlix.serialization.Cbor library is not currently compatible with DAG-CBOR
 */
class DagCborEncoder {

    private val bytes = mutableListOf<Byte>()

    // Note: this is only a subset of the available Cbor data headers
    val CBOR_UNSIGNED_NUMBER_HEADER: Int = 0b000_00000

    val CBOR_BYTE_STRING_HEADER: Int = 0b010_00000
    val CBOR_STRING_HEADER: Int = 0b011_00000

    val CBOR_ARRAY_HEADER: Int = 0b100_00000
    val CBOR_MAP_HEADER: Int = 0b101_00000

    val CBOR_SEMANTIC_HEADER: Int = 0b110_00000

    fun encodeNumber(number: Int): ByteArray {
        if (number < 24)
            return byteArrayOf(((CBOR_UNSIGNED_NUMBER_HEADER or number) and 0xFF).toByte())
        else throw IllegalArgumentException("Numbers larger than 23 are not currently supported")
    }

    fun encodeMap(map: Map<String, Any>): ByteArray {
        if (map.size < 24)
            return byteArrayOf(((CBOR_MAP_HEADER or map.size) and 0xFF).toByte()) +
                    map.map { (name, value) ->
                        encodeString(name) + when (value) {
                            is Int -> encodeNumber(value)
                            (value as? List<CID>) -> encodeCidArray(value.map { it.bytes })
                            else -> throw IllegalArgumentException("Unsupported type")
                        }
                    }.reduce { acc, bytes -> acc + bytes }
        else throw IllegalArgumentException("Maps containing more than 23 entries are not currently supported")
    }

    fun encodeString(string: String): ByteArray {
        if (string.length < 24)
            return byteArrayOf(((CBOR_STRING_HEADER or string.length) and 0xFF).toByte()) + string.encodeToByteArray()
        else throw IllegalArgumentException("Strings longer than 23 characters are not currently supported")
    }

    fun encodeByteString(bytes: ByteArray): ByteArray =
        if (bytes.size < 24)
            byteArrayOf(((CBOR_BYTE_STRING_HEADER or bytes.size) and 0xFF).toByte()) + bytes
        else if (bytes.size < 128)
            byteArrayOf(((CBOR_BYTE_STRING_HEADER or 24) and 0xFF).toByte()) + bytes.size.toByte() + bytes
        else throw IllegalArgumentException("Byte strings longer than 127 items are not currently supported")

    fun encodeCidArray(cids: List<ByteArray>): ByteArray {
        if (cids.size < 24)
            return byteArrayOf(((CBOR_ARRAY_HEADER or cids.size) and 0xFF).toByte()) +
                    cids.fold(byteArrayOf()) { acc, bytes ->
                        acc + encodeCid(bytes)
                    }
        else throw IllegalArgumentException("Lists containing more than 23 items are not currently supported")
    }

    fun encodeCid(cid: ByteArray): ByteArray {
        // CIDs are a Type 6 (semantic) CBOR atomic type with a value of 42
        // see here for more info: https://ipld.io/specs/codecs/dag-cbor/spec/#strictness
        val cidHeader = ((CBOR_SEMANTIC_HEADER or 24) and 0xFF).toByte()
        val cidTag = 42.toByte()

        // 0x00 is always placed before the CID bytes, for historical reasons
        val cidBytes = byteArrayOf(0) + cid

        return byteArrayOf(cidHeader, cidTag) + encodeByteString(cidBytes)
    }
}