package com.nft.gallery.diskcache

import com.nft.gallery.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    suspend fun insertAll(myMints: List<MyMint>) {
        if (myMints.isEmpty()) {
            return
        }

        val cachedIds = get(myMints.first().pubKey).map { it.id }.toSet()
        val newIds = myMints.map { it.id }.toSet()

        val staleData = cachedIds.filter { !newIds.contains(it) }

        myMintsDatabaseProvider.roomDb.myMintsDao().delete(staleData)

        myMintsDatabaseProvider.roomDb.myMintsDao().insertAll(myMints)
    }

    suspend fun get(pubKey: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = BuildConfig.RPC_CLUSTER.name
        )
}
