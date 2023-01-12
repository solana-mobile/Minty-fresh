package com.nft.gallery.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.usecase.*
import com.portto.solana.web3.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletViewState(
    val isLoading: Boolean = false,
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
    val memoTx: String = ""
)

val solanaUri = Uri.parse("https://solana.com")
val iconUri = Uri.parse("favicon.ico")
val identityName = "Solana"

@HiltViewModel
class WalletConnectionViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase
) : ViewModel() {

    private fun WalletViewState.updateViewState() {
        _state.update { this }
    }

    private val _state = MutableStateFlow(WalletViewState())

    val viewState: StateFlow<WalletViewState>
        get() = _state

    fun loadConnection() {
        val connection = persistenceUseCase.getWalletConnection()
        if (connection is Connected) {
            _state.value.copy(
                isLoading = true,
                canTransact = true,
                userAddress = connection.publicKey.toBase58(),
                userLabel = connection.accountLabel,
            ).updateViewState()
        }
    }

    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            val conn = persistenceUseCase.getWalletConnection()

            val currentConn = walletAdapter.transact(sender) {
                when (conn) {
                    is NotConnected -> {
                        val authed = authorize(solanaUri, iconUri, identityName, RpcCluster.Devnet)
                        Connected(
                            PublicKey(authed.publicKey),
                            authed.accountLabel ?: "",
                            authed.authToken
                        )
                    }
                    is Connected -> {
                        val reauthed = reauthorize(solanaUri, iconUri, identityName, conn.authToken)
                        Connected(
                            PublicKey(reauthed.publicKey),
                            reauthed.accountLabel ?: "",
                            reauthed.authToken
                        )
                    }
                }
            }

            persistenceUseCase.persistConnection(
                currentConn.publicKey,
                currentConn.accountLabel,
                currentConn.authToken
            )

        }
    }

    fun disconnect(sender: ActivityResultSender) {
        viewModelScope.launch {
            val conn = persistenceUseCase.getWalletConnection()

            if (conn is Connected) {
                walletAdapter.transact(sender) {
                    deauthorize(conn.authToken)
                }

                persistenceUseCase.clearConnection()

                WalletViewState().updateViewState()
            }
        }
    }
}