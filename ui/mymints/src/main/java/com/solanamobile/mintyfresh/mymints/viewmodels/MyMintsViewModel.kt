package com.solanamobile.mintyfresh.mymints.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solanamobile.mintyfresh.mymints.usecase.MyMintsUseCase
import com.solanamobile.mintyfresh.mymints.viewmodels.mapper.MyMintsMapper
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MyMintsViewState
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import com.solanamobile.mintyfresh.persistence.diskcache.MyMintsCacheRepository
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyMintsViewModel @Inject constructor(
    application: Application,
    private val myMintsMapper: MyMintsMapper,
    private val persistenceUseCase: WalletConnectionUseCase,
    private val myMintsCacheRepository: MyMintsCacheRepository
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<MyMintsViewState> =
        MutableStateFlow(myMintsMapper.mapLoading())

    val viewState = _viewState

    private var wasLoaded = false

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refresh() = viewModelScope.launch {
        _isRefreshing.update { true }
    }

    init {
        observeAllMints()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAllMints() {
        viewModelScope.launch {
            _isRefreshing.flatMapLatest { isRefreshing ->
                persistenceUseCase.walletDetails.map { isRefreshing to it }
            }.collect { (isRefreshing, userWalletDetails) ->
                if (userWalletDetails is Connected) {
                    try {
                        loadMyMints(userWalletDetails.publicKey, isRefreshing)
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                        _viewState.update { MyMintsViewState.Error(e) }
                    }
                    _isRefreshing.update { false }
                } else {
                    _isRefreshing.update { false }
                }
            }
        }

        viewModelScope.launch {
            persistenceUseCase.walletDetails.flatMapLatest { walletDetails ->
                val myMintsFlow = if (walletDetails is Connected) {
                    myMintsCacheRepository.get(
                        pubKey = walletDetails.publicKey.toString(),
                        rpcClusterName = RpcCluster.Devnet.name //TODO: This value will come from networking layer
                    )
                } else {
                    flow { emit(listOf()) }
                }

                myMintsFlow.map { walletDetails to it }
            }.collect { (userWalletDetail, myMints) ->
                if (userWalletDetail is Connected) {
                    if (myMints.isEmpty()) {
                        _viewState.update { MyMintsViewState.Empty() }
                    } else {
                        _viewState.update { MyMintsViewState.Loaded(myMints) }
                    }
                } else {
                    _viewState.update { MyMintsViewState.NoConnection() }
                }
            }
        }
    }

    private suspend fun loadMyMints(publicKey: PublicKey, forceRefresh: Boolean) {
        if (publicKey.toString().isEmpty() || (!forceRefresh && wasLoaded)) {
            _isRefreshing.update { false }
            return
        }

        if (forceRefresh) {
            val loadingMints =
                _viewState.value.myMints.filter { it.id.isNotEmpty() }.toMutableList()
                    .apply {
                        if (this.isEmpty()) {
                            // Add a loading placeholder to existing data.
                            for (i in 0 until 10) {
                                add(MyMint("", "", "", "", "", ""))
                            }
                        }
                    }

            // This update to insert loading placeholders. Note that some data is cached (Non loading state)
            _viewState.update { MyMintsViewState.Loading(loadingMints) }
        } else {
            if (_viewState.value is MyMintsViewState.Loading || _viewState.value is MyMintsViewState.Loaded) {
                return
            }
        }

        wasLoaded = true
        val mintsUseCase = MyMintsUseCase(publicKey)

        val nfts = mintsUseCase.getAllUserMintyFreshNfts()
        Log.d(TAG, "Found ${nfts.size} NFTs")

        val currentMintList = myMintsMapper.map(nfts)
        myMintsCacheRepository.deleteStaleData(
            currentMintList = currentMintList,
            publicKey.toString()
        )
        if (nfts.isNotEmpty()) {
            // Fetch and update each NFT data.
            nfts.forEach { nft ->
                val cachedMint = myMintsCacheRepository.get(
                    id = nft.mint.toString(),
                    pubKey = publicKey.toString(),
                    rpcClusterName = RpcCluster.Devnet.name //TODO: This value will come from networking layer
                )
                if (cachedMint == null) {
                    val metadata = mintsUseCase.getNftsMetadata(nft)
                    val mint = myMintsMapper.map(nft, metadata)
                    if (mint != null) {
                        myMintsCacheRepository.insertAll(listOf(mint))
                    }
                }
            }
        } else {
            // This update is needed because flow from roomDb wouldn't update above.
            _viewState.update { MyMintsViewState.Empty() }
        }
    }

    companion object {
        private const val TAG = "MyMintsViewModel"
    }
}