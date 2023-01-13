package com.nft.gallery.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.constant.mintyFreshCollectionName
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
    }

    private var _viewState = MutableStateFlow(mutableListOf<MyMint>())

    val viewState = _viewState

    var wasLoaded = false

    fun loadMyMints(publicKey: PublicKey, forceRefresh: Boolean = false) {
        if (publicKey.toString().isEmpty() || (!forceRefresh && wasLoaded)) {
            return
        }

        viewModelScope.launch {
            wasLoaded = true
            val mintsUseCase = MyMintsUseCase(publicKey)
            try {
                val nfts = mintsUseCase.getAllNftsForCollectionName(mintyFreshCollectionName)
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