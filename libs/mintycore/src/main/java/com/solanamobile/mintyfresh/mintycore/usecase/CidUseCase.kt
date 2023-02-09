package com.solanamobile.mintyfresh.mintycore.usecase

import com.solanamobile.mintyfresh.mintycore.ipld.CID
import io.ipfs.cid.Cid
import io.ipfs.multihash.Multihash
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class CidUseCase @Inject constructor() {

    fun getCidForFile(file: File): CID = getCid(file.readBytes())

    fun getCid(data: ByteArray): CID {
        // really silly that this ipfs library supports Multihash decode but not encode :face-palm:
        val fileHash = MessageDigest.getInstance("SHA-256").digest(data)
        return CID(Cid.buildCidV1(Cid.Codec.Raw, Multihash.Type.sha2_256, fileHash).toBytes())
    }
}