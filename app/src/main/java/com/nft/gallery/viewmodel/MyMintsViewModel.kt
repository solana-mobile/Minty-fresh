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
import kotlinx.coroutines.flow.asStateFlow
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

    private var _viewState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())
    private var _wasLaunched = false

    val viewState = _viewState.asStateFlow()
    val wasLaunched: Boolean
            get() = _wasLaunched

    fun loadMyMints(publicKey: PublicKey) {
        _wasLaunched = true
        viewModelScope.launch {
            val mintsUseCase = MyMintsUseCase(publicKey)
            try {
                val nfts = mintsUseCase.getAllNftsForCollectionName(MINTY_NFT_COLLECTION_NAME)
                Log.d(TAG, "Found ${nfts.size} NFTs")
                nfts.forEach { nft ->
                    val metadata = mintsUseCase.getNftsMetadata(nft)
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