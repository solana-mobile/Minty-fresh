package com.nft.gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.usecase.MyMintsUseCase
import com.nft.gallery.viewmodel.mapper.MyMintsMapper
import com.solana.core.PublicKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyMint(
    val id: String,
    val name: String?,
    val description: String?,
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

    private var _viewState: MutableMap<String, MutableStateFlow<List<MyMint>>> = mutableMapOf()

    val viewState = mutableMapOf<String, StateFlow<List<MyMint>>>()

    fun loadMyMints(publicKey: PublicKey, forceRefresh: Boolean = false) {
        if (publicKey.toString().isEmpty() || (!forceRefresh && _viewState.containsKey(publicKey.toString()))) {
            return
        }

        _viewState.putIfAbsent(publicKey.toString(), MutableStateFlow(mutableListOf()))

        viewModelScope.launch {
            val mintsUseCase = MyMintsUseCase(publicKey)
            try {
                val nfts = mintsUseCase.getAllNftsForCollectionName(MINTY_NFT_COLLECTION_NAME)
                Log.d(TAG, "Found ${nfts.size} NFTs")
                nfts.forEach { nft ->
                    val metadata = mintsUseCase.getNftsMetadata(nft)
                    Log.d(TAG, "Fetched ${nft.name} NFT metadata")
                    _viewState[publicKey.toString()]?.getAndUpdate {
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