package com.solanamobile.mintyfresh.mintycore.usecase

import com.solana.core.PublicKey
import io.ipfs.multibase.Multibase
import javax.inject.Inject

class DidUseCase @Inject constructor() {
    fun getDidForUser(user: PublicKey): String {
        val rawPubkey = user.toByteArray()
        // val ed25515H = "0xED"
        val ed25515VarInt = byteArrayOf(-19, 1) // varint encoded 0xED
        return "did:key:${Multibase.encode(Multibase.Base.Base58BTC, ed25515VarInt + rawPubkey)}"
    }
}