@file:UseSerializers(PublicKeyAs32ByteSerializer::class, U128Serializer::class)

package com.nft.gallery.metaplex.jen.shadowdrive

import com.metaplex.lib.serialization.serializers.solana.PublicKeyAs32ByteSerializer
import com.nft.gallery.metaplex.jen.U128
import com.nft.gallery.metaplex.jen.U128Serializer
import com.solana.core.PublicKey
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.UByte
import kotlin.UInt
import kotlin.ULong
import kotlin.UShort
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

//
// Accounts
//
// This code was generated locally by Funkatronics on 2023-01-24
//

@Serializable
class unstakeInfo(
    val timeLastUnstaked: Long,
    val epochLastUnstaked: ULong,
    val unstaker: PublicKey
)

@Serializable
class storageAccount(
    val isStatic: Boolean,
    val initCounter: UInt,
    val delCounter: UInt,
    val immutable: Boolean,
    val toBeDeleted: Boolean,
    val deleteRequestEpoch: UInt,
    val storage: ULong,
    val storageAvailable: ULong,
    val owner1: PublicKey,
    val owner2: PublicKey,
    val shdwPayer: PublicKey,
    val accountCounterSeed: UInt,
    val totalCostOfCurrentStorage: ULong,
    val totalFeesPaid: ULong,
    val creationTime: UInt,
    val creationEpoch: UInt,
    val lastFeeEpoch: UInt,
    val identifier: String
)

@Serializable
class storageAccountV2(
    val immutable: Boolean,
    val toBeDeleted: Boolean,
    val deleteRequestEpoch: UInt,
    val storage: ULong,
    val owner1: PublicKey,
    val accountCounterSeed: UInt,
    val creationTime: UInt,
    val creationEpoch: UInt,
    val lastFeeEpoch: UInt,
    val identifier: String
)

@Serializable
class userInfo(
    val accountCounter: UInt,
    val delCounter: UInt,
    val agreedToTos: Boolean,
    val lifetimeBadCsam: Boolean
)

@Serializable
class storageConfig(
    val shadesPerGib: ULong,
    val storageAvailable: U128,
    val tokenAccount: PublicKey,
    val admin2: PublicKey,
    val uploader: PublicKey,
    val mutableFeeStartEpoch: UInt?,
    val shadesPerGibPerEpoch: ULong,
    val crankBps: UShort,
    val maxAccountSize: ULong,
    val minAccountSize: ULong
)

@Serializable
class file(
    val immutable: Boolean,
    val toBeDeleted: Boolean,
    val deleteRequestEpoch: UInt,
    val size: ULong,
    val sha256Hash: List<UByte>,
    val initCounterSeed: UInt,
    val storageAccount: PublicKey,
    val name: String
)

//@Serializable
//class file(
//    val immutable: Boolean,
//    val toBeDeleted: Boolean,
//    val deleteRequestEpoch: UInt,
//    val size: ULong,
//    val sha256Hash: List<UByte>,
//    val initCounterSeed: UInt,
//    val storageAccount: PublicKey,
//    val name: String
//)
