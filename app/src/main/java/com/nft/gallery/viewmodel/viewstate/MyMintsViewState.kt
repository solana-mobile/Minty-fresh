package com.nft.gallery.viewmodel.viewstate

data class MyMint(
    val id: String,
    val name: String?,
    val description: String?,
    val mediaUrl: String,
)

sealed class MyMintsViewState(
    val myMints: List<MyMint> = listOf(),
) {

    class Loading(myMints: List<MyMint>) : MyMintsViewState(myMints)

    class Loaded(myMints: List<MyMint>) : MyMintsViewState(myMints)

    data class Empty(val message: String) : MyMintsViewState()

    data class Error(val error: Exception) : MyMintsViewState()
}