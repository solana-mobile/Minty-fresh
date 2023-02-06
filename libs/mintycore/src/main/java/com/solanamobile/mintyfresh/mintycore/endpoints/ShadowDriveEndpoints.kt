package com.solanamobile.mintyfresh.mintycore.endpoints

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST

interface ShadowDriveEndpoints {

    @Headers("Content-Type: application/json")
    @POST("storage-account")
    suspend fun createStorageAccount(@Body body: RequestBody): CreateAccountResponse

//    @Multipart
    @Headers("Content-Type: multipart/form")
    @POST("upload")
    suspend fun uploadFiles(@Body body: RequestBody): UploadResponse
}