package com.solanamobile.mintyfresh.mymints.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mymints.viewmodels.mapper.CacheToViewStateMapper
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MintedMedia
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MyMintsViewState
import com.solanamobile.mintyfresh.networkinterface.usecase.IMyMintsUseCase
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
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
    private val persistenceUseCase: WalletConnectionUseCase,
    private val myMintsUseCase: IMyMintsUseCase,
    private val viewStateMapper: CacheToViewStateMapper,
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<MyMintsViewState> =
        MutableStateFlow(
            MyMintsViewState.Loading(MutableList(10) { index -> MintedMedia(index.toString(), "", "", "") })
        )

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
                    myMintsUseCase.getCachedMints(walletDetails.publicKey)
                } else {
                    flow { emit(listOf()) }
                }

                myMintsFlow.map { walletDetails to it }
            }.collect { (userWalletDetail, myMints) ->
                if (userWalletDetail is Connected) {
                    if (myMints.isEmpty()) {
                        _viewState.update { MyMintsViewState.Empty() }
                    } else {
                        val mapped = myMints.map { viewStateMapper.mapMintToViewState(it) }
                        _viewState.update { MyMintsViewState.Loaded(mapped) }
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
            val loadingMints = _viewState.value.myMints
                .filter { it.id.isNotEmpty() }
                .toMutableList()
                .apply {
                    if (this.isEmpty()) {
                        listOf((0..9).map { MintedMedia() })
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

        val nfts = myMintsUseCase.getAllUserMintyFreshNfts(publicKey)
        if (nfts.isEmpty()) {
            _viewState.update { MyMintsViewState.Empty() }
        }
    }

    companion object {
        private const val TAG = "MyMintsViewModel"
    }
}