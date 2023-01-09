package com.nft.gallery.endpoints

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Part

interface NftStorageEndpoints {

    @POST("upload/")
    suspend fun uploadFile(@Part file: MultipartBody.Part, @Header("Authorization") token: String): ResponseBody
}