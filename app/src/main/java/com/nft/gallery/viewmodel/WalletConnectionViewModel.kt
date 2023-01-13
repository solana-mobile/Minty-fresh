package com.nft.gallery.viewmodel

import android.graphics.DiscretePathEffect
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.usecase.Connected
import com.nft.gallery.usecase.MyMintsUseCase
import com.nft.gallery.usecase.NotConnected
import com.nft.gallery.usecase.PersistenceUseCase
import com.nft.gallery.viewmodel.mapper.MyMintsMapper
import com.portto.solana.web3.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class WalletViewState(
    val isLoading: Boolean = false,
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
)

data class MyMint(
    val id: String,
    val name: String?,
    val description: String?,
    val mediaUrl: String,
)

val solanaUri: Uri = Uri.parse("https://solana.com")
val iconUri: Uri = Uri.parse("favicon.ico")
const val identityName = "Solana"

@HiltViewModel
class WalletConnectionViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase,
    private val myMintsMapper: MyMintsMapper,
) : ViewModel() {

    private fun WalletViewState.updateViewState() {
        _state.update { this }
    }

    private val _state = MutableStateFlow(WalletViewState())

    val viewState: StateFlow<WalletViewState>
        get() = _state

    companion object {
        private const val TAG = "MyMintsViewModel"

        // TODO real name and shared constant between the mint and the fetch
        private const val MINTY_NFT_COLLECTION_NAME = "Spaces NFT List"
    }

    private var _mintState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())

    val mintState = _mintState.asStateFlow()

    suspend fun loadConnection() {
        withContext(Dispatchers.IO) {
            val connection = persistenceUseCase.getWalletConnection()
            if (connection is Connected) {
                _state.value.copy(
                    isLoading = true,
                    canTransact = true,
                    userAddress = connection.publicKey.toBase58(),
                    userLabel = connection.accountLabel,
                ).updateViewState()
                loadMyMints(com.solana.core.PublicKey(connection.publicKey.toBase58()))
            }
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
                        try {
                            val reauthed =
                                reauthorize(solanaUri, iconUri, identityName, conn.authToken)
                            Connected(
                                PublicKey(reauthed.publicKey),
                                reauthed.accountLabel ?: "",
                                reauthed.authToken
                            )
                        } catch (exception: Exception) {
                            persistenceUseCase.clearConnection()
                            persistenceUseCase.getWalletConnection()
                        }
                    }
                }
            }

            if (currentConn is Connected) {
                persistenceUseCase.persistConnection(
                    currentConn.publicKey,
                    currentConn.accountLabel,
                    currentConn.authToken
                )

                _state.value.copy(
                    isLoading = false,
                    canTransact = false,
                    userAddress = currentConn.publicKey.toBase58(),
                    userLabel = currentConn.accountLabel,
                ).updateViewState()
                loadMyMints(com.solana.core.PublicKey(currentConn.publicKey.toBase58()))
            } else {
                _state.value.copy(
                    isLoading = false,
                    canTransact = true,
                    solBalance = 0.0,
                    userAddress = "",
                    userLabel = ""
                ).updateViewState()
                _mintState.update { listOf() }
            }
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
                _mintState.update { listOf() }

                WalletViewState().updateViewState()
            }
        }
    }

    private fun loadMyMints(publicKey: com.solana.core.PublicKey) {
        viewModelScope.launch {
            val mintsUseCase = MyMintsUseCase(publicKey)
            try {
                val nfts = mintsUseCase.getAllNftsForCollectionName(MINTY_NFT_COLLECTION_NAME)
                Log.d(TAG, "Found ${nfts.size} NFTs")
                nfts.forEach { nft ->
                    val metadata = mintsUseCase.getNftsMetadata(nft)
                    Log.d(TAG, "Fetched ${nft.name} NFT metadata")
                        _mintState.getAndUpdate {
                        it.toMutableList().apply {
                            myMintsMapper.map(nft, metadata)?.let { myMint ->
                                add(myMint)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }
}