package com.nft.gallery.diskcache

import com.nft.gallery.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao()
            .insertAll(myMints)

    suspend fun get(pubKey: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = BuildConfig.RPC_CLUSTER.name
        )

    suspend fun deleteAll() = myMintsDatabaseProvider.roomDb.myMintsDao().deleteAll()
}
