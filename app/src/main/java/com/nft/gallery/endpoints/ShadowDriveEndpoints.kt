/*
 * ShadowDriveEndpoints
 * Gallery
 * 
 * Created by Funkatronics on 1/24/2023
 */

package com.nft.gallery.endpoints

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ShadowRequest(
    val transaction: String
)

interface ShadowDriveEndpoints {

    @Headers("Content-Type: application/json")
    @POST("storage-account")
    suspend fun createStorageAccount(@Body request: ShadowRequest): ShadowDriveResponse
}