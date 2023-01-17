package com.nft.gallery.diskcache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nft.gallery.BuildConfig

@Dao
interface MyMintsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(myMints: List<MyMint>)

    @Query("SELECT * FROM MyMint WHERE rpc_cluster = :clusterName AND pub_key = :pubKey")
    suspend fun get(pubKey: String, clusterName: String): List<MyMint>

    @Query("DELETE FROM MyMint")
    suspend fun deleteAll()
}
