/*
 * MobileWalletIdentityDriver
 * Gallery
 * 
 * Created by Funkatronics on 1/11/2023
 */

package com.nft.gallery.metaplex

import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.solana.core.PublicKey
import com.solana.core.Transaction

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
        val txnBytes = transaction.serialize(com.solana.core.SerializeConfig(requireAllSignatures = false))
        mobileWalletSignTransaction(txnBytes) { signedBytes ->
            onComplete(Result.success(Transaction.from(signedBytes)))
        }
    }

    abstract fun mobileWalletSignTransaction(transaction: ByteArray, onComplete: (ByteArray) -> Unit)
}