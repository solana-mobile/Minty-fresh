package com.nft.gallery.usecase

import com.nft.gallery.repository.LatestBlockhashRepository
import com.nft.gallery.repository.MintTransactionRepository
import com.nft.gallery.repository.SendTransactionRepository
import com.nft.gallery.repository.StorageUploadRepository
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.*
import com.solanamobile.mintyfresh.core.peristence.usecase.Connected
import com.solanamobile.mintyfresh.core.peristence.usecase.PersistenceUseCase
import com.solanamobile.mintyfresh.core.walletconnection.viewmodel.mintyFreshIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface MintState {
    object None : MintState
    object UploadingMedia : MintState
    object CreatingMetadata : MintState
    object BuildingTransaction : MintState
    object AwaitingConfirmation : MintState
    class Signing(val transaction: ByteArray) : MintState
    class Minting(val mintAddress: PublicKey) : MintState
    class Complete(val transactionSignature: String) : MintState
    class Error(val message: String): MintState
}

class PerformMintUseCase @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val storageRepository: StorageUploadRepository,
    private val persistenceUseCase: PersistenceUseCase,
    private val mintTransactionRepository: MintTransactionRepository,
    private val blockhashRepository: LatestBlockhashRepository,
    private val sendTransactionRepository: SendTransactionRepository
) {

    private val _mintState = MutableStateFlow<MintState>(MintState.None)

    val mintState: StateFlow<MintState> = _mintState

    suspend fun performMint(sender: ActivityResultSender,
                            title: String, desc: String, filePath: String) =
        withContext(Dispatchers.IO) {
            val authToken = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.authToken else null
            }.stateIn(this).value

            val creator = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.publicKey else null
            }.stateIn(this).value

            check(creator != null)

            // upload the media file
            _mintState.value = MintState.UploadingMedia

            val nftImageUrl = storageRepository.uploadFile(filePath)

            // create and upload the NFT metadata
            _mintState.value = MintState.CreatingMetadata

            val metadataUrl = storageRepository.uploadMetadata(title, desc, nftImageUrl)

            // begin building the transaction
            _mintState.value = MintState.BuildingTransaction

            val mintAccount = HotAccount()
            val mintTxn = mintTransactionRepository.buildMintTransaction(title, metadataUrl, mintAccount.publicKey, creator)

            mintTxn.setRecentBlockHash(blockhashRepository.getLatestBlockHash())

            val transactionBytes =
                mintTxn.serialize(SerializeConfig(
                    requireAllSignatures = false,
                    verifySignatures = false
                ))

            // begin signing transaction step
            _mintState.value = MintState.Signing(transactionBytes)

            val params = mintyFreshIdentity //BLOCK: Get this from relevant layers

            val txResult = walletAdapter.transact(sender) {
                authToken?.let {
                    reauthorize(params.identityUri, params.iconUri, params.identityName, authToken)
                } ?: authorize(params.identityUri, params.iconUri, params.identityName, RpcCluster.Devnet)  //Block cluster from networking layer

                val signingResult = signTransactions(arrayOf(transactionBytes))

                return@transact signingResult.signedPayloads[0].sliceArray(1 until 1 + SIGNATURE_LENGTH)
            }

            when (txResult) {
                is TransactionResult.Success -> {
                    txResult.successPayload?.let { primarySignature ->
                        // rebuild transaction object from signed bytes
                        // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
                        // build up the Message (and Transaction) object manually (for now)
                        // val signed = Transaction.from(signedBytes)
                        val signed = Transaction().apply {
                            setRecentBlockHash(mintTxn.recentBlockhash)
                            feePayer = creator
                            addInstruction(*mintTxn.instructions.toTypedArray())
                            addSignature(creator, primarySignature)
                        }

                        // now that the primary signer (creator) has signed, the mint account can sign
                        signed.partialSign(mintAccount)

                        _mintState.value = MintState.Minting(mintAccount.publicKey)

                        // send the signed transaction to the cluster
                        val transactionSignature = sendTransactionRepository.sendTransaction(signed)

                        _mintState.value = MintState.AwaitingConfirmation

                        // Await for transaction confirmation
                        sendTransactionRepository.confirmTransaction(transactionSignature)

                        _mintState.value = MintState.Complete(transactionSignature)
                    }
                }
                is TransactionResult.Failure -> {
                    _mintState.value = MintState.Complete(txResult.message)
                }
                else -> { }
            }
        }
}