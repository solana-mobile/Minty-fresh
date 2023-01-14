package com.nft.gallery.diskcache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyMintsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(myMints: List<MyMint>)

    @Query("SELECT * FROM MyMint")
    suspend fun get(): List<MyMint>

    @Query("DELETE FROM MyMint")
    suspend fun deleteAll()
}
