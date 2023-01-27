package com.solanamobile.mintyfresh.core.walletconnection.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solanamobile.mintyfresh.core.peristence.usecase.Connected
import com.solanamobile.mintyfresh.core.peristence.usecase.NotConnected
import com.solanamobile.mintyfresh.core.peristence.usecase.PersistenceUseCase
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
    val noWallet: Boolean = false
)

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

    fun connect(connectionParams: ConnectionParams, activityResultSender: ActivityResultSender) {
        viewModelScope.launch {
            val result = walletAdapter.transact(activityResultSender) {
                authorize(
                    identityUri = connectionParams.identityUri,
                    iconUri = connectionParams.iconUri,
                    identityName = connectionParams.identityName,
                    rpcCluster = connectionParams.rpcCluster
                )
            }

            when (result) {
                is TransactionResult.Success -> {
                    persistenceUseCase.persistConnection(
                        PublicKey(result.payload.publicKey),
                        result.payload.accountLabel ?: "",
                        result.payload.authToken
                    )
                }
                is TransactionResult.NoWalletFound -> {
                    _state.update {
                        _state.value.copy(
                            noWallet = true,
                            canTransact = true
                        )
                    }
                }
                is TransactionResult.Failure -> {
                    /** not gonna do anything here now **/
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            persistenceUseCase.clearConnection()
        }
    }
}