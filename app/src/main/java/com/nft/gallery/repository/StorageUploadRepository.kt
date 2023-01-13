package com.nft.gallery.repository

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import com.nft.gallery.metaplex.JsonMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class StorageUploadRepository @Inject constructor(
    private val endpoints: NftStorageEndpoints
) {

    suspend fun uploadFile(filePath: String): String {
        return withContext(Dispatchers.IO) {
            val uploadFile = File(filePath)
            val reqBody = uploadFile.asRequestBody()

            upload(reqBody)
        }
    }

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

            upload(body)
        }
    }

    private suspend fun upload(body: RequestBody): String {
        return withContext(Dispatchers.IO) {

            val result = endpoints.uploadFile(body, token)

            result.error?.let { err ->
                throw Error("NFT.Storage returned error: ${err.name}: $${err.message}")
            }

            "https://${result.value?.cid}${ipfsUrlSuffix}"
        }
    }

    companion object {
        const val token = "Bearer ${BuildConfig.NFTSTORAGE_KEY}"
        const val ipfsUrlSuffix = ".ipfs.nftstorage.link"
    }
}