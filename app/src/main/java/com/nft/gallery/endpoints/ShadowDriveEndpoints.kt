/*
 * ShadowDriveEndpoints
 * Gallery
 * 
 * Created by Funkatronics on 1/24/2023
 */

package com.nft.gallery.endpoints

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ShadowDriveEndpoints {

    @POST("storage-account")
    suspend fun createStorageAccount(@Body json: RequestBody): ShadowDriveResponse
}