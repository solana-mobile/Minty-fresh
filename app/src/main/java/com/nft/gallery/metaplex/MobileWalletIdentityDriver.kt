package com.nft.gallery.metaplex

import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.solana.core.*

abstract class MobileWalletIdentityWrapper : IdentityDriver {
    abstract val publicKeyBytes: ByteArray
    override val publicKey: PublicKey
        get() = PublicKey.readPubkey(publicKeyBytes, 0)

    override fun sendTransaction(
        transaction: Transaction,
        recentBlockHash: String?,
        onComplete: (Result<String>) -> Unit
    ) = Unit

    override fun signAllTransactions(
        transactions: List<Transaction>,
        onComplete: (Result<List<Transaction?>>) -> Unit
    )  = Unit

    // only signed transaction is actually used by Metaplex
    override fun signTransaction(
        transaction: Transaction,
        onComplete: (Result<Transaction>) -> Unit
    ) {
        transaction.feePayer = publicKey
        val txnBytes = transaction.serialize(SerializeConfig(requireAllSignatures = false, verifySignatures = false))
        mobileWalletSignTransaction(txnBytes) { signedBytes ->
            // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
            // build up the Message (and Transaction) object manually (for now)
            // onComplete(Result.success(Transaction.from(signedBytes)))

            val mwaSignature = signedBytes.sliceArray(1 until 1 + SIGNATURE_LENGTH)

            val txn = Transaction().apply {
                setRecentBlockHash(transaction.recentBlockhash)
                feePayer = publicKey
                addInstruction(*transaction.instructions.toTypedArray())
                addSignature(publicKey, mwaSignature)
            }

            onComplete(Result.success(txn))
        }
    }

    abstract fun mobileWalletSignTransaction(transaction: ByteArray, onComplete: (ByteArray) -> Unit)
}