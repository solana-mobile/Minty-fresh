package com.nft.gallery.endpoints

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NftStorageEndpoints {

    @POST("upload/")
    suspend fun uploadFile(@Body file: RequestBody, @Header("Authorization") token: String): ResponseBody
}