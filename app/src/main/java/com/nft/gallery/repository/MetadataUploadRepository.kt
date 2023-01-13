/*
 * MetadataUploadRepository
 * Gallery
 * 
 * Created by Funkatronics on 1/12/2023
 */

package com.nft.gallery.repository

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import com.nft.gallery.metaplex.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class MetadataUploadRepository @Inject constructor(
    private val endpoints: NftStorageEndpoints
) {

    suspend fun uploadMetadata(name: String, description: String, imageUrl: String): String {
        return withContext(Dispatchers.IO) {

            val json = Json.encodeToString(
                JsonMetadata(
                    name = name,
                    description = description,
                    image = imageUrl,
                    attributes = listOf(
                        JsonMetadata.Attribute("Minty Fresh", "true")
                    ),
                    properties = JsonMetadata.Properties(
                        files = listOf(
                            JsonMetadata.Properties.File(imageUrl, "image/png")
                        ),
                        category = "image"
                    )
                )
            )

            val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val metadataJson = endpoints.uploadFile(body, token) as Map<*, *>

            val cid = (metadataJson["value"] as? Map<*, *>)?.get("cid")
            "https://${cid}${ipfsUrlSuffix}"
        }
    }

    companion object {
        const val token = "Bearer ${BuildConfig.NFTSTORAGE_KEY}"
        const val ipfsUrlSuffix = ".ipfs.nftstorage.link"
    }
}