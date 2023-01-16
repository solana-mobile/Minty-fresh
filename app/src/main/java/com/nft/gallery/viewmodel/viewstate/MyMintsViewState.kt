package com.nft.gallery.viewmodel.viewstate

import com.nft.gallery.diskcache.MyMint

sealed class MyMintsViewState(
    val myMints: List<MyMint> = listOf(),
) {

    class Loading(myMints: List<MyMint>) : MyMintsViewState(myMints)

    class Loaded(myMints: List<MyMint>) : MyMintsViewState(myMints)

    data class Empty(val message: String) : MyMintsViewState()

    data class Error(val error: Exception) : MyMintsViewState()
}