package com.solanamobile.mintyfresh.mintycore.endpoints

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NftStorageEndpoints {

    @POST("upload/")
    suspend fun uploadFile(@Body file: RequestBody, @Header("Authorization") token: String): NftStorageResponse

    @Headers("Content-Type: application/car")
    @POST("metaplex/upload/")
    suspend fun uploadCar(@Body file: RequestBody, @Header("x-web3auth") token: String): NftStorageResponse
}