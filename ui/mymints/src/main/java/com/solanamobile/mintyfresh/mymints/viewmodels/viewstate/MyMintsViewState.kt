package com.solanamobile.mintyfresh.mymints.viewmodels.viewstate

import com.solanamobile.mintyfresh.networkinterface.data.MintedMedia

sealed class MyMintsViewState(
    val myMints: List<MintedMedia> = listOf(),
) {

    class Loading(myMints: List<MintedMedia>) : MyMintsViewState(myMints)

    class Loaded(myMints: List<MintedMedia>) : MyMintsViewState(myMints)

    class Empty : MyMintsViewState()

    class NoConnection : MyMintsViewState()

    data class Error(val error: Exception) : MyMintsViewState()
}