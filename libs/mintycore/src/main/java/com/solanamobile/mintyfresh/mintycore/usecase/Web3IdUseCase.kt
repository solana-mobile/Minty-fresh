package com.solanamobile.mintyfresh.mintycore.usecase

import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.ipld.CID
import io.ipfs.cid.Cid
import io.ipfs.multibase.Multibase
import io.ipfs.multihash.Multihash
import java.security.MessageDigest
import javax.inject.Inject

class Web3IdUseCase @Inject constructor() {

    fun getContentId(data: ByteArray): CID {
        // really silly that this ipfs library supports Multihash decode but not encode :face-palm:
        val fileHash = MessageDigest.getInstance("SHA-256").digest(data)
        return CID(Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, fileHash).toBytes())
    }

    fun getRootContentId(data: ByteArray): CID {
        // really silly that this ipfs library supports Multihash decode but not encode :face-palm:
        val fileHash = MessageDigest.getInstance("SHA-256").digest(data)
        return CID(Cid.buildCidV1(Cid.Codec.DagProtobuf, Multihash.Type.sha2_256, fileHash).toBytes())
    }

    fun getDecentralizedIdForUser(user: PublicKey): String {
        val rawPubkey = user.toByteArray()
        val ed25515VarInt = byteArrayOf(-19, 1) // varint encoded 0xED
        return "did:key:${Multibase.encode(Multibase.Base.Base58BTC, ed25515VarInt + rawPubkey)}"
    }
}