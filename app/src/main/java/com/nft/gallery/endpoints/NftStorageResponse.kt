package com.nft.gallery.endpoints

import kotlinx.serialization.Serializable

@Serializable
data class NftStorageResponse(
    val ok: Boolean,
    val value: FileData?,
    val error: Error? = null
)

@Serializable
data class FileData(
    val cid: String,
    val size: Int
)

@Serializable
data class Error(
    val name: String,
    val message: String
)