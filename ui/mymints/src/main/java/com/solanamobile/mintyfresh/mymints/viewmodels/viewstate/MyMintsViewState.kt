package com.solanamobile.mintyfresh.mymints.viewmodels.viewstate

import com.solanamobile.mintyfresh.mymints.diskcache.MyMint

sealed class MyMintsViewState(
    val myMints: List<MyMint> = listOf(),
) {

    class Loading(myMints: List<MyMint>) : MyMintsViewState(myMints)

    class Loaded(myMints: List<MyMint>) : MyMintsViewState(myMints)

    class Empty : MyMintsViewState()

    class NoConnection : MyMintsViewState()

    data class Error(val error: Exception) : MyMintsViewState()
}