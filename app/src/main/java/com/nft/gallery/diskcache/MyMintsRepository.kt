package com.nft.gallery.diskcache

import com.nft.gallery.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao().insertAll(myMints)

    suspend fun deleteStaleData(currentMintList: List<MyMint>, pubKey: String) {
        val newIds = currentMintList.map { it.id }.toSet()
        val cachedIds = get(pubKey).map { it.id }.toSet()

        val staleData = cachedIds.filter { !newIds.contains(it) }
        myMintsDatabaseProvider.roomDb.myMintsDao().delete(staleData)
    }

    suspend fun get(pubKey: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = BuildConfig.RPC_CLUSTER.name
        ).sortedBy { it.id }
}
