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
import kotlinx.coroutines.flow.getAndUpdate
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

    var wasLoaded = false

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
                _viewState.value = myMintsMapper.mapLoading()
            }

            val cachedNfts = myMintsRepository.get()
            if (cachedNfts.isNotEmpty()) {
                _viewState.getAndUpdate {
                    cachedNfts.toMutableList()
                }
                if (!forceRefresh && cachedNfts.isNotEmpty())
                    return@launch
            }

            wasLoaded = true
            val mintsUseCase = MyMintsUseCase(publicKey)

            try {
                val nfts = mintsUseCase.getAllNftsForCollectionName(mintyFreshCollectionName)
                Log.d(TAG, "Found ${nfts.size} NFTs")

                if (nfts.isEmpty()) {
                    _viewState.value =
                        MyMintsViewState.Empty("No mints yet. Start minting pictures with Minty Fresh!")
                } else {
                    _viewState.value = MyMintsViewState.Loaded(myMintsMapper.map(nfts))
                    nfts.forEachIndexed { index, nft ->
                        val metadata = mintsUseCase.getNftsMetadata(nft)

                    Log.d(TAG, "Fetched ${nft.name} NFT metadata")
                    _viewState.getAndUpdate {
                        val myNfts = myMintsViewState.myMints.apply {
                            myMintsMapper.map(nft, metadata)?.let { myMint ->
                                this[index] = myMint
                            }
                        }
                        myMintsRepository.insertAll(myNfts)
                        MyMintsViewState.Loaded(myNfts)
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