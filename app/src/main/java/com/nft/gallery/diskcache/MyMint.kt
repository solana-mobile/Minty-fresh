package com.nft.gallery.diskcache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MyMint")
data class MyMint(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "nft_name") val name: String?,
    @ColumnInfo(name = "nft_description") val description: String?,
    @ColumnInfo(name = "nft_media_url") val mediaUrl: String,
)