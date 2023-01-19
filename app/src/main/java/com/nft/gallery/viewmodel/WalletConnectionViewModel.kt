package com.nft.gallery.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.BuildConfig
import com.nft.gallery.usecase.Connected
import com.nft.gallery.usecase.NotConnected
import com.nft.gallery.usecase.PersistenceUseCase
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletViewState(
    val isLoading: Boolean = false,
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
)

val solanaUri: Uri = Uri.parse("https://solana.com")
val iconUri: Uri = Uri.parse("favicon.ico")
const val identityName = "Solana"

@HiltViewModel
class WalletConnectionViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WalletViewState())

    val viewState: StateFlow<WalletViewState>
        get() = _state

    init {
        viewModelScope.launch {
            persistenceUseCase.walletDetails
                .collect { walletDetails ->
                    val detailState = when (walletDetails) {
                        is Connected -> {
                            WalletViewState(
                                isLoading = false,
                                canTransact = true,
                                solBalance = 0.0,
                                userAddress = walletDetails.publicKey.toBase58(),
                                userLabel = walletDetails.accountLabel
                            )
                        }
                        is NotConnected -> WalletViewState()
                    }

                    _state.value = detailState
                }
        }
    }

    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            walletAdapter.transact(sender) {

                val authed = authorize(solanaUri, iconUri, identityName, BuildConfig.RPC_CLUSTER)

                persistenceUseCase.persistConnection(PublicKey(authed.publicKey), authed.accountLabel ?: "", authed.authToken)
            }

        }
    }

    fun disconnect() {
        viewModelScope.launch {
            persistenceUseCase.clearConnection()
        }
    }
}