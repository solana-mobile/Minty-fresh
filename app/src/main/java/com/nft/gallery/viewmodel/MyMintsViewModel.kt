package com.nft.gallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyMint(
    val id: String,
    val mediaUrl: String,
)

@HiltViewModel
class MyMintsViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())

    val viewState = _viewState.asStateFlow()

    fun loadMyMints() {
        viewModelScope.launch {
            //Get persisted user address, hit metaplex API for NFTs created by them
        }
    }
}