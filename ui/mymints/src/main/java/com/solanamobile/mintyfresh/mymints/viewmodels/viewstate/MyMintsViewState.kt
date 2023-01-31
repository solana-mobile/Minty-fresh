package com.solanamobile.mintyfresh.mymints.viewmodels.viewstate

data class MintedMedia(
    val id: String = "",
    val mediaUrl: String = "",
    val name: String = "",
    val description: String = ""
)

sealed class MyMintsViewState(
    val myMints: List<MintedMedia> = listOf(),
) {

    class Loading(myMints: List<MintedMedia>) : MyMintsViewState(myMints)

    class Loaded(myMints: List<MintedMedia>) : MyMintsViewState(myMints)

    class Empty : MyMintsViewState()

    class NoConnection : MyMintsViewState()

    data class Error(val error: Exception) : MyMintsViewState()
}