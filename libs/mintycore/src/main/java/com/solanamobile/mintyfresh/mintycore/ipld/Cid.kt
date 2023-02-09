package com.solanamobile.mintyfresh.mintycore.ipld

import io.ipfs.multibase.Multibase
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@kotlinx.serialization.Serializable
data class CID(val bytes: ByteArray)

fun CID.toCanonicalString(): String = Multibase.encode(Multibase.Base.Base32, bytes)

// Not currently used but keeping for now
object CIDSerializer : KSerializer<CID> {
    override val descriptor = ByteArraySerializer().descriptor

    override fun serialize(encoder: Encoder, value: CID) =
//        encoder.encodeSerializableValue(ByteArraySerializer(), byteArrayOf(0xd8.toByte(), 0x2a, 0x00))
        encoder.encodeSerializableValue(
            ByteArraySerializer(),
            byteArrayOf(-40, 42, 0x00) + value.bytes)

    override fun deserialize(decoder: Decoder): CID {
        val bytes = decoder.decodeSerializableValue(ByteArraySerializer())
        return CID(bytes)
    }
}