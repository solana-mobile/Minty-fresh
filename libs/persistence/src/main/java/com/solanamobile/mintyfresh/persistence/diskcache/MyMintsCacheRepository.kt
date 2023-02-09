package com.solanamobile.mintyfresh.persistence.diskcache

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Disk storage backed MyMints cache implementation.
 * Reads are exclusively from local storage to support offline access.
 */
@Singleton
class MyMintsCacheRepository @Inject constructor(
    private val myMintsDatabaseProvider: MyMintsDatabaseProvider
) {

    /**
     * Inserts [myMints] into the database .
     */
    suspend fun insertAll(myMints: List<MyMint>) =
        myMintsDatabaseProvider.roomDb.myMintsDao().insertAll(myMints)

    /**
     * Deletes stale data for [pubKey] and [clusterName] which is not in [currentMintList].
     */
    suspend fun deleteStaleData(
        currentMintList: List<MyMint>,
        clusterName: String,
        pubKey: String
    ) {
        val newIds = currentMintList.map { it.id }
        myMintsDatabaseProvider.roomDb.myMintsDao()
            .deleteStaleData(pubKey, clusterName = clusterName, latestNftIds = newIds)
    }

    /**
     * Stream of [MyMint] for given [pubKey] and [clusterName]
     */
    fun get(pubKey: String, clusterName: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            pubKey = pubKey,
            clusterName = clusterName
        )

    /**
     * Returns [MyMint] for given (NFT id [id]), (PublicKey [pubKey]) and [clusterName]
     */
    suspend fun get(id: String, pubKey: String, clusterName: String) =
        myMintsDatabaseProvider.roomDb.myMintsDao().get(
            id = id,
            pubKey = pubKey,
            clusterName = clusterName
        )
}
