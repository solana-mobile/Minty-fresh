package com.solanamobile.mintyfresh.walletconnectbutton.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.NotConnected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
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
    private val walletConnectionUseCase: WalletConnectionUseCase,
    private val rpcConfig: IRpcConfig
) : ViewModel() {

    private val _state = MutableStateFlow(WalletViewState())

    val viewState: StateFlow<WalletViewState>
        get() = _state

    init {
        viewModelScope.launch {
            walletConnectionUseCase.walletDetails
                .collect { walletDetails ->
                    val detailState = when (walletDetails) {
                        is Connected -> {
                            WalletViewState(
                                isLoading = false,
                                canTransact = true,
                                solBalance = 0.0,
                                userAddress = walletDetails.publicKey,
                                userLabel = walletDetails.accountLabel
                            )
                        }
                        is NotConnected -> WalletViewState()
                    }

                    _state.value = detailState
                }
        }
    }

    fun connect(
        identityUri: Uri,
        iconUri: Uri,
        identityName: String,
        activityResultSender: ActivityResultSender
    ) {
        viewModelScope.launch {
            val result = walletAdapter.transact(activityResultSender) {
                authorize(
                    identityUri = identityUri,
                    iconUri = iconUri,
                    identityName = identityName,
                    rpcCluster = rpcConfig.rpcCluster
                )
            }

            when (result) {
                is TransactionResult.Success -> {
                    walletConnectionUseCase.persistConnection(
                        result.payload.publicKey,
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
            walletConnectionUseCase.clearConnection()
        }
    }
}