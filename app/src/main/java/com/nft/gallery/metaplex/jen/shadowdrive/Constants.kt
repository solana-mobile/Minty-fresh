package com.nft.gallery.metaplex.jen.shadowdrive

import com.nft.gallery.metaplex.jen.U128
import com.solana.core.PublicKey
import java.math.BigInteger
import kotlin.Long
import kotlin.UByte
import kotlin.UInt
import kotlin.ULong
import kotlin.UShort

//
// Constants
//
// This code was generated locally by Funkatronics on 2023-01-24
//

val INITIAL_STORAGE_COST: ULong = 1_073_741_824u

val MAX_IDENTIFIER_SIZE: Int = 64

val INITIAL_STORAGE_AVAILABLE: U128 = U128.fromBigInteger(BigInteger("109951162777600"))

val BYTES_PER_GIB: UInt = 1_073_741_824u

val MAX_ACCOUNT_SIZE: ULong = 1_099_511_627_776u

val MIN_ACCOUNT_SIZE: ULong = 1024u

val MAX_FILENAME_SIZE: Int = 32

val SHA256_HASH_SIZE: Int = 256 / 8

val MAX_URL_SIZE: Int = 256

val DELETION_GRACE_PERIOD: UByte = 1u

val UNSTAKE_TIME_PERIOD: Long = 0 * 24 * 60 * 60

val UNSTAKE_EPOCH_PERIOD: ULong = 1u

val INITIAL_CRANK_FEE_BPS: UShort = 100u

// manually added code
/// Address of the Mainnet Shadow Drive Program.
val PROGRAM_ADDRESS: PublicKey = PublicKey("2e1wdyNhUvE76y6yUCvah2KaviavMJYKoRun8acMRBZZ")
/// Address of the Mainnet Shadow Token Mint.
val TOKEN_MINT: PublicKey = PublicKey("SHDWyBxihqiCj6YekG2GUr7wqKLeLAMK1gHZck9pL6y")
/// Address of the upload authority for the Mainnet Shadow Drive Program.
val UPLOADER: PublicKey = PublicKey("972oJTFyjmVNsWM4GHEGPWUomAiJf2qrVotLtwnKmWem")
/// Address that handles token emissions for the Mainnet Shadow Drive Program.
val EMISSIONS: PublicKey = PublicKey("SHDWRWMZ6kmRG9CvKFSD7kVcnUqXMtd3SaMrLvWscbj")

/// Endpoint that is used for file uploads and fetching object data.
const val SHDW_DRIVE_ENDPOINT = "https://shadow-storage.genesysgo.net"
const val SHDW_DRIVE_OBJECT_PREFIX = "https://shdw-drive.genesysgo.net"

const val FILE_SIZE_LIMIT: ULong = 1_073_741_824u //1GB

/// Program Derived Address that holds storage config parameters and admin pubkeys for the Mainnet Shadow Drive Program.
val STORAGE_CONFIG_PDA = PublicKey.findProgramAddress(
    listOf("storage-config".toByteArray(Charsets.UTF_8)),
    PROGRAM_ADDRESS
)