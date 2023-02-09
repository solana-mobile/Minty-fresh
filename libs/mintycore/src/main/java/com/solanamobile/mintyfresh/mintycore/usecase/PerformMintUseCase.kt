package com.solanamobile.mintyfresh.mintycore.usecase

import android.net.Uri
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
import com.solanamobile.mintyfresh.mintycore.ipld.*
import com.solanamobile.mintyfresh.mintycore.repository.LatestBlockhashRepository
import com.solanamobile.mintyfresh.mintycore.repository.MintTransactionRepository
import com.solanamobile.mintyfresh.mintycore.repository.SendTransactionRepository
import com.solanamobile.mintyfresh.mintycore.repository.StorageUploadRepository
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val carFileUseCase: CarFileUseCase,
    private val web3AuthUseCase: XWeb3AuthUseCase,
    private val storageRepository: StorageUploadRepository,
    private val persistenceUseCase: WalletConnectionUseCase,
    private val mintTransactionRepository: MintTransactionRepository,
    private val blockhashRepository: LatestBlockhashRepository,
    private val sendTransactionRepository: SendTransactionRepository,
    private val rpcConfig: IRpcConfig
) {

    private val _mintState = MutableStateFlow<MintState>(MintState.None)

    val mintState: StateFlow<MintState> = _mintState

    suspend fun performMint(identityUri: Uri,
                            iconUri: Uri,
                            identityName: String,
                            sender: ActivityResultSender,
                            title: String,
                            desc: String,
                            filePath: String
    ) = withContext(Dispatchers.IO) {

        val authToken = persistenceUseCase.walletDetails.map {
            if (it is Connected) it.authToken else null
        }.stateIn(this).value

        val walletAddy = persistenceUseCase.walletDetails.map {
            if (it is Connected) it.publicKey else null
        }.stateIn(this).value

        check(walletAddy != null)

        val creator = PublicKey(walletAddy)

        //region create and upload the media file
        // first build a car file, we need to know the root cid so we can sign it later
        val imageCar = carFileUseCase.buildNftImageCar(filePath)

        // TODO: need to move this signing step to abstraction
        val xWeb3MessageImage = web3AuthUseCase
            .buildxWeb3AuthMessage(creator, imageCar.rootCids.first().toCanonicalString())

        // begin message transaction step
        _mintState.value = MintState.Signing(xWeb3MessageImage.encodeToByteArray())
        delay(700)

        val signature1 = walletAdapter.transact(sender) {
            authToken?.let {
                reauthorize(identityUri, iconUri, identityName, authToken)
            } ?: authorize(identityUri, iconUri, identityName, rpcConfig.rpcCluster)

            val signingResult = signMessages(arrayOf(xWeb3MessageImage.toByteArray()), arrayOf(creator.pubkey))

            return@transact signingResult.signedPayloads[0]
        }

        val xWeb3TokenImage = web3AuthUseCase
            .buildXWeb3AuthToken(xWeb3MessageImage, signature1.successPayload!!)

        // upload the media file
        _mintState.value = MintState.UploadingMedia

        // now we can upload the image car file, using the web3 auth token we just made
        val nftImageUrl = storageRepository.uploadCar(imageCar.build(), xWeb3TokenImage)
        //endregion

        //region create and upload the NFT metadata
        _mintState.value = MintState.CreatingMetadata

        // first build a car file, we need to know the root cid so we can sign it later
        val metadataCar = carFileUseCase.buildNftMetadataCar(title, desc, nftImageUrl)

        // TODO: need to move this signing step to abstraction
        val xWeb3Message = web3AuthUseCase
            .buildxWeb3AuthMessage(creator, metadataCar.rootCids.first().toCanonicalString())

        // begin message transaction step
        _mintState.value = MintState.Signing(xWeb3Message.encodeToByteArray())
        delay(700)

        val signature2 = walletAdapter.transact(sender) {
            authToken?.let {
                reauthorize(identityUri, iconUri, identityName, authToken)
            } ?: authorize(identityUri, iconUri, identityName, rpcConfig.rpcCluster)

            val signingResult = signMessages(arrayOf(xWeb3Message.toByteArray()), arrayOf(creator.pubkey))

            return@transact signingResult.signedPayloads[0]
        }

        val xWeb3Token = web3AuthUseCase
            .buildXWeb3AuthToken(xWeb3Message, signature2.successPayload!!)

        // create and upload the NFT metadata
        _mintState.value = MintState.CreatingMetadata

        // now we can upload the metadata car file, using the web3 auth token we just made
        val metadataUrl = storageRepository.uploadCar(metadataCar.build(), xWeb3Token)
        //endregion

        // begin building the mint transaction
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
        delay(700)

        val txResult = walletAdapter.transact(sender) {
            authToken?.let {
                reauthorize(identityUri, iconUri, identityName, authToken)
            } ?: authorize(identityUri, iconUri, identityName, rpcConfig.rpcCluster)

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
                _mintState.value = MintState.Error(txResult.message)
            }
            else -> { }
        }
    }
}