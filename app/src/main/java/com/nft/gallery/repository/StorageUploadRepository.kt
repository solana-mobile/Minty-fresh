package com.nft.gallery.repository

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class StorageUploadRepository @Inject constructor(
    private val endpoints: NftStorageEndpoints
) {

    suspend fun uploadFile(filePath: String): String {
        return withContext(Dispatchers.IO) {
            val uploadFile = File(filePath)
            val reqBody = uploadFile.asRequestBody()

            val result = endpoints.uploadFile(reqBody, token)

            "https://${result.value.cid}${ipfsUrlSuffix}"
        }
    }

    companion object {
        const val token = "Bearer ${BuildConfig.NFTSTORAGE_KEY}"
        const val ipfsUrlSuffix = ".ipfs.nftstorage.link"
    }
}