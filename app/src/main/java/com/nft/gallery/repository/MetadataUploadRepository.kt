/*
 * MetadataUploadRepository
 * Gallery
 * 
 * Created by Funkatronics on 1/12/2023
 */

package com.nft.gallery.repository

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class MetadataUploadRepository @Inject constructor(
    private val endpoints: NftStorageEndpoints
) {

    suspend fun uploadMetadata(name: String, description: String, imageUrl: String,
                               symbol : String = "MF", sellerFeeBasisPoints: Long = 0): String {
        return withContext(Dispatchers.IO) {

            val body = ("{\n" +
                    "  \"name\": \"$name\",\n" +
                    "  \"symbol\": \"$symbol\",\n" +
                    "  \"description\": \"$description\",\n" +
                    "  \"seller_fee_basis_points\": $sellerFeeBasisPoints,\n" +
                    "  \"image\": \"${imageUrl}\",\n" +
//                    "  \"external_url\": \"https://solana.com\",\n" +
                    "  \"attributes\": [\n" +
                    "    {\n" +
                    "      \"trait_type\": \"Minty Fresh\",\n" +
                    "      \"value\": \"true\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"trait_type\": \"mobile\",\n" +
                    "      \"value\": \"yes\"\n" +
                    "   },\n" +
                    "   {\n" +
                    "      \"trait_type\": \"Saga\",\n" +
                    "      \"value\": \"oh yeah\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"properties\": {\n" +
                    "    \"files\": [\n" +
                    "      {\n" +
                    "        \"uri\": \"${imageUrl}\",\n" +
                    "        \"type\": \"image/png\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"category\": \"image\"\n" +
                    "  }\n" +
                    "}").toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())


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