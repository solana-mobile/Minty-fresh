package com.nft.gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.constant.mintyFreshCollectionName
import com.nft.gallery.diskcache.MyMint
import com.nft.gallery.diskcache.MyMintsRepository
import com.nft.gallery.usecase.Connected
import com.nft.gallery.usecase.MyMintsUseCase
import com.nft.gallery.usecase.PersistenceUseCase
import com.nft.gallery.viewmodel.mapper.MyMintsMapper
import com.nft.gallery.viewmodel.viewstate.MyMintsViewState
import com.solana.core.PublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyMintsViewModel @Inject constructor(
    application: Application,
    private val myMintsMapper: MyMintsMapper,
    private val persistenceUseCase: PersistenceUseCase,
    private val myMintsRepository: MyMintsRepository
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<MyMintsViewState> =
        MutableStateFlow(myMintsMapper.mapLoading())

    val viewState = _viewState

    private var wasLoaded = false

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refresh() = viewModelScope.launch {
        _isRefreshing.update { true }
        loadMyMints(forceRefresh = true)
        _isRefreshing.update { false }
    }

    init {
        loadMyMints()
    }

    fun loadMyMints(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            persistenceUseCase.walletDetails
                .collect {
                    if (it is Connected) {
                        loadMyMints(it.publicKey, forceRefresh)
                    } else {
                        _viewState.value =
                            MyMintsViewState.Empty("Connect your wallet to see your mints")
                    }
                }
        }
    }

    private fun loadMyMints(publicKey: PublicKey, forceRefresh: Boolean) {
        if (publicKey.toString().isEmpty() || (!forceRefresh && wasLoaded)) {
            return
        }

        viewModelScope.launch {
            if (forceRefresh) {
                val loadingMints =
                    _viewState.value.myMints.filter { it.id.isNotEmpty() }.toMutableList().apply {
                        val loadingSize = if (this.isEmpty()) 10 else 1
                        // Add a loading placeholder to existing data.
                        for (i in 0 until loadingSize) {
                            add(MyMint("", "", "", "", "", ""))
                        }
                    }
                _viewState.update {
                    MyMintsViewState.Loaded(loadingMints)
                }
            } else {
                val cachedNfts = myMintsRepository.get(publicKey.toString())
                if (cachedNfts.isNotEmpty()) {
                    _viewState.update {
                        MyMintsViewState.Loaded(cachedNfts)
                    }
                    return@launch
                }
            }

            wasLoaded = true
            val mintsUseCase = MyMintsUseCase(publicKey)

            try {
                // TODO: Sort by mint date
                val nfts = mintsUseCase.getAllNftsForCollectionName(mintyFreshCollectionName)
                    .sortedBy { it.mint.toString() }
                Log.d(TAG, "Found ${nfts.size} NFTs")

                if (nfts.isEmpty()) {
                    _viewState.update {
                        MyMintsViewState.Empty("No mints yet. Start minting pictures with Minty Fresh!")
                    }
                } else {
                    val currentMintList = myMintsMapper.map(nfts)
                    myMintsRepository.deleteStaleData(
                        currentMintList = currentMintList,
                        publicKey.toString()
                    )
                    val currentCachedData = myMintsRepository.get(publicKey.toString())
                    val loadingData = currentCachedData.toMutableList().apply {
                        // Add the exact number of loaders based on the total number of mints.
                        for (i in 0 until currentMintList.size - currentCachedData.size) {
                            add(MyMint("", "", "", "", "", ""))
                        }
                    }
                    _viewState.update {
                        MyMintsViewState.Loaded(loadingData)
                    }
                    // Fetch and update each NFT data.
                    nfts.forEachIndexed { index, nft ->
                        val metadata = mintsUseCase.getNftsMetadata(nft)

                        Log.d(TAG, "Fetched ${nft.name} NFT metadata")
                        _viewState.getAndUpdate { myMintsViewState ->
                            val myNfts = myMintsViewState.myMints.toMutableList().apply {
                                myMintsMapper.map(nft, metadata)?.let { myMint ->
                                    this[index] = myMint
                                }
                            }

                            // Inserting in database when we fetched all the NFTs
                            if (index == nfts.size - 1) {
                                myMintsRepository.insertAll(myNfts)
                            }
                            // Update view state as we receive data.
                            MyMintsViewState.Loaded(myNfts)
                        }
                    }
                    // Remove loading placeholders if necessary.
                    _viewState.update {
                        MyMintsViewState.Loaded(_viewState.value.myMints.filter { it.id.isNotEmpty() })
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                _viewState.value = MyMintsViewState.Error(e)
            }
        }
    }

    companion object {
        private const val TAG = "MyMintsViewModel"
    }
}