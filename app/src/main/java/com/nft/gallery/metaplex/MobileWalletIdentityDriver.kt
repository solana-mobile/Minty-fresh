/*
 * MobileWalletIdentityDriver
 * Gallery
 * 
 * Created by Funkatronics on 1/11/2023
 */

package com.nft.gallery.metaplex

import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.nft.gallery.viewmodel.iconUri
import com.nft.gallery.viewmodel.identityName
import com.nft.gallery.viewmodel.solanaUri
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SigningState {
    object None: SigningState
    class InProgress(val transaction: ByteArray): SigningState
    class Complete(val signedTransaction: ByteArray): SigningState
}

class MobileWalletIdentityDriver(
    override val publicKey: PublicKey,
    private val authToken: String,
    private val sender: ActivityResultSender,
    private val coroutineScope: CoroutineScope
) : IdentityDriver {

    private val _signingState = MutableStateFlow<SigningState>(SigningState.None)
    val signingState: StateFlow<SigningState> = _signingState

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

        coroutineScope.launch {

            _signingState.value = SigningState.InProgress(txnBytes)

            val signedBytes = MobileWalletAdapter().transact(sender) {
                reauthorize(solanaUri, iconUri, identityName, authToken)
                val result = signTransactions(arrayOf(txnBytes))
                return@transact result.signedPayloads[0]
            }

            _signingState.value = SigningState.Complete(signedBytes)

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
}

suspend fun buildSigningIdentityDriver(publicKey: PublicKey,
                                       signingBlock: (suspend (ByteArray) -> ByteArray)) =
    object : IdentityDriver {
        override val publicKey: PublicKey
            get() = publicKey

        override fun sendTransaction(
            transaction: Transaction,
            recentBlockHash: String?,
            onComplete: (Result<String>) -> Unit
        ) = Unit

        override fun signAllTransactions(
            transactions: List<Transaction>,
            onComplete: (Result<List<Transaction?>>) -> Unit
        ) = Unit

        // only signed transaction is actually used by Metaplex
        override fun signTransaction(
            transaction: Transaction,
            onComplete: (Result<Transaction>) -> Unit
        ) {
            transaction.feePayer = publicKey
            val txnBytes = transaction.serialize(
                SerializeConfig(
                    requireAllSignatures = false,
                    verifySignatures = false
                )
            )

            CoroutineScope(Dispatchers.Default).launch {
                val signedBytes = signingBlock.invoke(txnBytes)

                // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
                // build up the Message (and Transaction) object manually (for now)
                // onComplete(Result.success(Transaction.from(signedBytes)))

                val mwaSignature = signedBytes.sliceArray(1 until 1 + SIGNATURE_LENGTH)

                // need to build a new transaction
                val txn = Transaction().apply {
                    setRecentBlockHash(transaction.recentBlockhash)
                    feePayer = publicKey
                    addInstruction(*transaction.instructions.toTypedArray())
                    addSignature(publicKey, mwaSignature)
                }

                onComplete(Result.success(txn))
            }
        }
    }