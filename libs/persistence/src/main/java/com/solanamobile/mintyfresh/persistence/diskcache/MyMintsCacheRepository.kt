package com.solanamobile.mintyfresh.persistence.diskcache

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsCacheRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao().insertAll(myMints)

    suspend fun deleteStaleData(currentMintList: List<MyMint>, pubKey: String) {
        val newIds = currentMintList.map { it.id }
        myMintsDatabaseProvider.roomDb.myMintsDao().deleteStaleData(pubKey, newIds)
    }

    fun get(pubKey: String, rpcClusterName: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = rpcClusterName
        )

    suspend fun get(id: String, pubKey: String, rpcClusterName: String) = myMintsDatabaseProvider.roomDb.myMintsDao().get(
        id = id,
        pubKey = pubKey,
        clusterName = rpcClusterName
    )
}
