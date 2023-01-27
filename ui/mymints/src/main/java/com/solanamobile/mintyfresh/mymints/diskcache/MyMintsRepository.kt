package com.solanamobile.mintyfresh.mymints.diskcache

import com.solana.mobilewalletadapter.clientlib.RpcCluster
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider,
    private val rpcCluster: RpcCluster
) {

    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao().insertAll(myMints)

    suspend fun deleteStaleData(currentMintList: List<MyMint>, pubKey: String) {
        val newIds = currentMintList.map { it.id }
        myMintsDatabaseProvider.roomDb.myMintsDao().deleteStaleData(pubKey, newIds)
    }

    fun get(pubKey: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = rpcCluster.name
        )

    suspend fun get(id: String, pubKey: String) = myMintsDatabaseProvider.roomDb.myMintsDao().get(
        id = id,
        pubKey = pubKey,
        clusterName = rpcCluster.name
    )
}
