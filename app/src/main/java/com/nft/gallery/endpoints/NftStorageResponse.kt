package com.nft.gallery.endpoints

data class NftStorageResponse(
    val ok: Boolean,
    val value: FileData
)

data class FileData(
    val cid: String,
    val size: Int
)