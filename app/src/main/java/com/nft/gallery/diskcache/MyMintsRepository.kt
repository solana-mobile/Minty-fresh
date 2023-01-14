package com.nft.gallery.diskcache

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao()
            .insertAll(myMints)

    suspend fun get() = myMintsDatabaseProvider.roomDb.myMintsDao().get()

}
