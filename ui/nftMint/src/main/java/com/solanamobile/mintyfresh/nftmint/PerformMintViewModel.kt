package com.solanamobile.mintyfresh.nftmint

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solanamobile.mintyfresh.mintycore.usecase.MintState
import com.solanamobile.mintyfresh.mintycore.usecase.PerformMintUseCase
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
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
    private val persistenceUseCase: WalletConnectionUseCase,
    private val performMintUseCase: PerformMintUseCase,
    private val mobileWalletAdapter: MobileWalletAdapter
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(
        PerformMintViewState()
    )

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
    fun performMint(
        identityUri: Uri,
        iconUri: Uri,
        identityName: String,
        sender: ActivityResultSender,
        title: String,
        desc: String,
        filePath: String
    ) {
        viewModelScope.launch {
            if (!_viewState.value.isWalletConnected) {
                val result = mobileWalletAdapter.transact(sender) {
                    authorize(identityUri, iconUri, identityName, RpcCluster.Devnet)   //Cluster from networking layer
                }

                if (result !is TransactionResult.Success) {
                    _viewState.update {
                        _viewState.value.copy(mintState = MintState.Error("Could not connect to wallet."))
                    }
                    return@launch
                }

                persistenceUseCase.persistConnection(PublicKey(result.payload.publicKey), result.payload.accountLabel ?: "", result.payload.authToken)
            }

            performMintUseCase.performMint(identityUri, iconUri, identityName, sender, title, desc, filePath)
        }
    }
}