package com.solanamobile.mintyfresh.persistence.diskcache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MyMint::class],
    version = 2,
)
abstract class MyMintsDatabase : RoomDatabase() {
    abstract fun myMintsDao(): MyMintsDao

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM MyMint")
    }
}
