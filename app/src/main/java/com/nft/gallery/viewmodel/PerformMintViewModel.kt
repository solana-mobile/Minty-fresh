package com.nft.gallery.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.repository.StorageUploadRepository
import com.nft.gallery.usecase.*
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MintState {
    NONE,
    UPLOADING_FILE,
    CREATING_METADATA,
    MINTING,
    SIGNING,
    COMPLETE
}

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintState: MintState = MintState.NONE
)

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application,
    private val storageRepository: StorageUploadRepository,
    private val persistenceUseCase: PersistenceUseCase,
    private val mintNftUseCase: BuildMintTransactionUseCase,
    private val blockhashUseCase: GetLatestBlockhashUseCase,
    private val sendTransactionUseCase: SendTransactionUseCase
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(PerformMintViewState())

    val viewState: StateFlow<PerformMintViewState> = _viewState.asStateFlow()

    val authToken =
        persistenceUseCase.walletDetails.map {
            (it as? Connected)?.authToken
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val publicKey =
        persistenceUseCase.walletDetails.map {
            when (it) {
                is Connected -> it.publicKey
                is NotConnected -> null
            }.also { pubkey ->
                _viewState.update {
                    _viewState.value.copy(isWalletConnected = pubkey != null)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)


    // duplicated from WalletConnectionViewModel - would it be better to inject that vm here and call it?
    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            MobileWalletAdapter().transact(sender) {

                // TODO: need to change to mainnet, or intelligently pick based on the RPC url
                //  being used (currently using BuildConfig.SOLANA_RPC_URL)
                val authed = authorize(solanaUri, iconUri, identityName, RpcCluster.Devnet)

                persistenceUseCase.persistConnection(PublicKey(authed.publicKey), authed.accountLabel ?: "", authed.authToken)
            }
        }
    }

    /**
     * We should perhaps think about updating the ViewState with form input, then it wouldn't
     * have to be passed here. Also we'll want to support dynamic attributes in the future.
     */
    fun performMint(sender: ActivityResultSender, title: String, desc: String, imgUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {

            // TODO: handle case if public key is null?
            val publicKey = publicKey.value ?: return@launch
            val authToken = authToken.value ?: return@launch

            // upload the media file
            _viewState.update {
                _viewState.value.copy(mintState = MintState.UPLOADING_FILE)
            }

            val nftImageUrl = storageRepository.uploadFile(imgUrl)

            // create and upload the NFT metadata
            _viewState.update {
                _viewState.value.copy(mintState = MintState.CREATING_METADATA)
            }

            val metadataUrl = storageRepository.uploadMetadata(title, desc, nftImageUrl)

            // TODO: should we ad another state here for "Building Transaction..."?

            val mintAccount = HotAccount()
            val mintTxn = mintNftUseCase.buildMintTransaction(title, metadataUrl, mintAccount.publicKey, publicKey)

            mintTxn.setRecentBlockHash(blockhashUseCase.getlatestBlockHash())

            // begin signing transaction step
            _viewState.update {
                _viewState.value.copy(mintState = MintState.SIGNING)
            }
            delay(700)

            val primarySignature = MobileWalletAdapter().transact(sender) {
                reauthorize(solanaUri, iconUri, identityName, authToken)

                val transactionBytes =
                    mintTxn.serialize(SerializeConfig(
                        requireAllSignatures = false,
                        verifySignatures = false
                    ))

                val signingResult = signTransactions(arrayOf(transactionBytes))

                return@transact signingResult.signedPayloads[0].sliceArray(1 until 1 + SIGNATURE_LENGTH)
            }

            // rebuild transaction object from signed bytes
            // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
            // build up the Message (and Transaction) object manually (for now)
            // val signed = Transaction.from(signedBytes)
            val signed = Transaction().apply {
                setRecentBlockHash(mintTxn.recentBlockhash)
                feePayer = publicKey
                addInstruction(*mintTxn.instructions.toTypedArray())
                addSignature(publicKey, primarySignature)
            }

            // now that the primary signer (creator) has signed, the mint account can sign
            signed.partialSign(mintAccount)

            _viewState.update {
                _viewState.value.copy(
                    mintState = MintState.MINTING
                )
            }

            // send the signed transaction to the cluster
            val transactionSignature = sendTransactionUseCase.sendTransaction(signed)

            // Await for transaction confirmation
            sendTransactionUseCase.confirmTransaction(transactionSignature)

            // TODO: we should do something here ie show the user their newly minted NFT

            _viewState.update {
                _viewState.value.copy(
                    mintState = MintState.COMPLETE
                )
            }
        }
    }

    interface StartActivityForResultSender {
        fun startActivityForResult(intent: Intent, onActivityCompleteCallback: () -> Unit) // throws ActivityNotFoundException
    }
}