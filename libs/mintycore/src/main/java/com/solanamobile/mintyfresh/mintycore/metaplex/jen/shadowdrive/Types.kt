@file:UseSerializers(PublicKeyAs32ByteSerializer::class, U128Serializer::class)

package com.solanamobile.mintyfresh.mintycore.metaplex.jen.shadowdrive

import com.metaplex.lib.serialization.serializers.solana.PublicKeyAs32ByteSerializer
import com.solanamobile.mintyfresh.mintycore.metaplex.jen.U128Serializer
import kotlinx.serialization.UseSerializers

//
// Types
//
// This code was generated locally by Funkatronics on 2023-01-24
//
enum class Mode {
    Increment,

    Decrement,

    Initialize
}
