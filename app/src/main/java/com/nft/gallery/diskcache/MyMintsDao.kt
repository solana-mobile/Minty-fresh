package com.nft.gallery.diskcache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MyMintsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(myMints: List<MyMint>)

    @Query("SELECT * FROM MyMint WHERE rpc_cluster = :clusterName AND pub_key = :pubKey ORDER BY id")
    fun get(pubKey: String, clusterName: String): Flow<List<MyMint>>

    @Query("SELECT * FROM MyMint WHERE rpc_cluster = :clusterName AND pub_key = :pubKey AND id = :id LIMIT 1")
    fun get(id: String, pubKey: String, clusterName: String): MyMint?

    @Query("DELETE FROM MyMint WHERE pub_key = :pubKey AND id NOT IN (:latestNftIds)")
    suspend fun deleteStaleData(pubKey: String, latestNftIds: List<String>)
}
