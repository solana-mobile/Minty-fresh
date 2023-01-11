package com.nft.gallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintingInProgress: Boolean = true
)

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(PerformMintViewState())

    val viewState: StateFlow<PerformMintViewState> = _viewState.asStateFlow()

    /**
     * We should perhaps think about updating the ViewState with form input, then it wouldn't
     * have to be passed here. Also we'll want to support dynamic attributes in the future.
     */
    fun performMint(title: String, desc: String, imgUrl: String) {
        viewModelScope.launch {

        }
    }

}