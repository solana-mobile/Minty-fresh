package com.nft.gallery.repository

import android.util.Log
import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Inject

class StorageUploadRepository @Inject constructor(

) {
    private val token = "Bearer ${BuildConfig.NFTSTORAGE_KEY}"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.nft.storage/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val endpoints = retrofit.create(NftStorageEndpoints::class.java)

    suspend fun uploadFile(filePath: String) {
        withContext(Dispatchers.IO) {
            val uploadFile = File(filePath)
            val reqBody = uploadFile.asRequestBody()

            val result = endpoints.uploadFile(reqBody, token)

            Log.v("Andrew", result.value.cid)
        }
    }
}