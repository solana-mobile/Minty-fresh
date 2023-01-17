package com.nft.gallery.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.BuildConfig
import com.nft.gallery.usecase.*
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintState: MintState = MintState.NONE
)

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application,
    private val persistenceUseCase: PersistenceUseCase,
    private val performMintUseCase: PerformMintUseCase
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

    init {
        viewModelScope.launch {
            performMintUseCase.mintState.collect { mintState ->
                _viewState.update {
                    _viewState.value.copy(mintState = mintState)
                }
            }
        }
    }


    // duplicated from WalletConnectionViewModel - would it be better to inject that vm here and call it?
    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            MobileWalletAdapter().transact(sender) {
                val authed = authorize(solanaUri, iconUri, identityName, BuildConfig.RPC_CLUSTER)

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
            val creatorPublicKey = publicKey.value ?: return@launch //connect(sender)
            val authToken = authToken.value ?: return@launch

            performMintUseCase.performMint(sender, creatorPublicKey, authToken, title, desc, imgUrl)
        }
    }

    interface StartActivityForResultSender {
        fun startActivityForResult(intent: Intent, onActivityCompleteCallback: () -> Unit) // throws ActivityNotFoundException
    }
}