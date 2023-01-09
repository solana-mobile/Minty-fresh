package com.nft.gallery.repository

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

class StorageUploadRepository @Inject constructor(

) {
    private val token = "Bearer ${BuildConfig.NFTSTORAGE_KEY}"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.nft.storage/")
        .build()

    private val endpoints = retrofit.create(NftStorageEndpoints::class.java)

    suspend fun uploadFile(filePath: String) {
        val uploadFile = File(filePath)

        val reqBody = uploadFile.asRequestBody("multipart/form-file".toMediaTypeOrNull())
        val partToUpload = MultipartBody.Part.createFormData("file", uploadFile.name, reqBody)

        val response = endpoints.uploadFile(partToUpload, token)
    }
}