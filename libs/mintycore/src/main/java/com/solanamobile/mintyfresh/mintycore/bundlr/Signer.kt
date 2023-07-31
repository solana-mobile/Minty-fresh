package com.solanamobile.mintyfresh.mintycore.bundlr

enum class SignatureConfig(
    val signatureType: Short,
    val signatureLength: Int,
    val ownerLength: Int
) {
    ED25519(2, 64, 32);

    companion object {
        operator fun get(index: Int): SignatureConfig? {
            return if (index == 2) ED25519 else null
        }
    }
}

interface Signer {
    val publicKey: ByteArray
    val signatureType: Short
    val signatureLength: Int
    val ownerLength: Int

    suspend fun sign(message: ByteArray): ByteArray
}

abstract class Ed25519Signer : Signer {
    override val signatureType: Short = SignatureConfig.ED25519.signatureType
    override val signatureLength: Int = SignatureConfig.ED25519.signatureLength
    override val ownerLength: Int = SignatureConfig.ED25519.ownerLength
}