package com.nft.gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.repository.NFTMintyRepository
import com.solana.core.PublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyMint(
    val id: String,
    val mediaUrl: String,
)

@HiltViewModel
class MyMintsViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    companion object {
        private val TAG = "MyMintsViewModel"
    }

    private var _viewState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())

    val viewState = _viewState.asStateFlow()

    fun loadMyMints(publicKey: PublicKey) {
        viewModelScope.launch {
            val nftMintyRepository = NFTMintyRepository(publicKey)
            _viewState.value = listOf()
            try {
                val nfts = nftMintyRepository.getAllNftsFromMinty()
                nfts.forEach { nft ->
                    nftMintyRepository.getNftsMetadata(nft).image?.let { imageUrl ->
                        _viewState.getAndUpdate {
                            it.toMutableList().apply {
                                add(
                                    MyMint(nft.mint.toString(), imageUrl)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // TODO?
                Log.e(TAG, e.toString())
            }
        }
    }
}