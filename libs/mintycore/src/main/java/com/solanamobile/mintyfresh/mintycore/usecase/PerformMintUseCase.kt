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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

        // create upload files for both metadata and image
        _mintState.value = MintState.CreatingMetadata

        val fullCar = carFileUseCase.buildNftCar(title, desc, filePath)

        val xWeb3Message = web3AuthUseCase
            .buildxWeb3AuthMessage(creator, fullCar.rootCid.toCanonicalString())

        // begin message transaction step
        _mintState.value = MintState.Signing(xWeb3Message.encodeToByteArray())
        delay(700)

        val signatureResult = walletAdapter.transact(sender) {
            authToken?.let {
                reauthorize(identityUri, iconUri, identityName, authToken)
            } ?: authorize(identityUri, iconUri, identityName, rpcConfig.rpcCluster)

            val signingResult = signMessages(arrayOf(xWeb3Message.encodeToByteArray()), arrayOf(creator.pubkey))

            return@transact signingResult.signedPayloads[0]
        }

        val signature = signatureResult.successPayload ?: run {
            _mintState.value = MintState.Error("Wallet signature failed")
            return@withContext
        }

        val web3AuthToken = web3AuthUseCase.buildXWeb3AuthToken(xWeb3Message, signature)

        // now we can upload the car files, using the web3 auth tokens we just made
        _mintState.value = MintState.UploadingMedia

        // upload the media file
        val directoryUrl = storageRepository.uploadCar(fullCar.serialize(), web3AuthToken)

        // TODO: should get this some other way, or get a direct link via the metadata cid
        val metadataUrl = "$directoryUrl/$title.json"

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