package com.nft.gallery.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.BuildConfig
import com.nft.gallery.appName
import com.nft.gallery.iconUri
import com.nft.gallery.identityUri
import com.nft.gallery.usecase.Connected
import com.nft.gallery.usecase.MintState
import com.nft.gallery.usecase.PerformMintUseCase
import com.nft.gallery.usecase.PersistenceUseCase
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintState: MintState = MintState.None
)

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application,
    private val persistenceUseCase: PersistenceUseCase,
    private val performMintUseCase: PerformMintUseCase
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(PerformMintViewState())

    val viewState: StateFlow<PerformMintViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                performMintUseCase.mintState,
                persistenceUseCase.walletDetails
            ) { mintState, walletDetails ->
                PerformMintViewState(
                    isWalletConnected = walletDetails is Connected,
                    mintState = mintState
                )
            }.collect { newState ->
                _viewState.update { newState }
            }
        }
    }

    /**
     * We should perhaps think about updating the ViewState with form input, then it wouldn't
     * have to be passed here. Also we'll want to support dynamic attributes in the future.
     */
    fun performMint(sender: ActivityResultSender, title: String, desc: String, imgUrl: String) {
        viewModelScope.launch {
            if (!_viewState.value.isWalletConnected) {
                MobileWalletAdapter().transact(sender) {
                    val authed = authorize(identityUri, iconUri, appName, BuildConfig.RPC_CLUSTER)

                    persistenceUseCase.persistConnection(PublicKey(authed.publicKey), authed.accountLabel ?: "", authed.authToken)
                }
            }

            performMintUseCase.performMint(sender, title, desc, imgUrl)
        }
    }

    interface StartActivityForResultSender {
        fun startActivityForResult(intent: Intent, onActivityCompleteCallback: () -> Unit) // throws ActivityNotFoundException
    }
}