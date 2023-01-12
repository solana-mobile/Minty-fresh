package com.nft.gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.repository.NFTMintyRepository
import com.nft.gallery.viewmodel.mapper.MyMintsMapper
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
    private val myMintsMapper: MyMintsMapper,
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MyMintsViewModel"

        // TODO real name and shared constant between the mint and the fetch
        private const val MINTY_NFT_COLLECTION_NAME = "Spaces NFT List"
    }

    private var _viewState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())

    val viewState = _viewState.asStateFlow()

    fun loadMyMints(publicKey: PublicKey) {
        viewModelScope.launch {
            val nftMintyRepository = NFTMintyRepository(publicKey)
            _viewState.value = listOf()
            try {
                val nfts = nftMintyRepository.getAllNftsFromMinty(MINTY_NFT_COLLECTION_NAME)
                Log.d(TAG, "Found ${nfts.size} NFTs")
                nfts.forEach { nft ->
                    val metadata = nftMintyRepository.getNftsMetadata(nft)
                    Log.d(TAG, "Fetched ${nft.name} NFT metadata")
                    _viewState.getAndUpdate {
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