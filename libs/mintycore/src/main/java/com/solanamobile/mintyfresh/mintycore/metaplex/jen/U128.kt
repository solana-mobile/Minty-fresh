package com.solanamobile.mintyfresh.mintycore.metaplex.jen

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

data class U128(val bytes: ByteArray) {
    init {
        require(bytes.size in 0..128)
    }

    companion object {
        @JvmStatic fun fromBigInteger(bigInt: BigInteger) = U128(bigInt.toByteArray())
    }
}

object U128Serializer : KSerializer<U128> {
    override val descriptor: SerialDescriptor = ByteArraySerializer().descriptor

    override fun deserialize(decoder: Decoder): U128 = U128(ByteArray(16) {
        decoder.decodeByte()
    })


    override fun serialize(encoder: Encoder, value: U128) {
        value.bytes.forEach {
            encoder.encodeByte(it)
        }
    }
}