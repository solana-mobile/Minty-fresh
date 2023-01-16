package com.nft.gallery.diskcache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MyMint::class], version = 1)
abstract class MyMintsDatabase : RoomDatabase() {
    abstract fun myMintsDao(): MyMintsDao
}
