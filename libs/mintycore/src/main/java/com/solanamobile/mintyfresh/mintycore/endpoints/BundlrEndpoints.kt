package com.solanamobile.mintyfresh.mintycore.endpoints

import com.solanamobile.mintyfresh.mintycore.bundlr.Bundle
import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import retrofit2.http.*

interface BundlrEndpoints {

    /*
     * NOTE: solana is hardcoded here, but could be made dynamic (via PATH(...))
     *  so that other chains could be used.
     */

    @GET("info/")
    suspend fun info(): NodeInfoResponse

    @GET("price/solana/{NUM_BYTES}")
    suspend fun price(@Path("NUM_BYTES") numBytes: Int): Long

    @GET("account/balance/solana")
    suspend fun balance(@Query("address") address: String): Long

    @POST("account/balance/solana")
    @Headers("Content-Type: application/json")
    suspend fun fund(@Body txId: String): Boolean

    @POST("tx/solana/")
    @Headers("Content-Type: application/octet-stream")
    suspend fun upload(@Body data: ByteArray): UploadResponse

    @POST("tx/solana/")
    @Headers("Content-Type: application/octet-stream")
    suspend fun uploadDataItem(@Body dataItem: DataItem): UploadResponse

    @POST("tx/solana/")
    @Headers("Content-Type: application/octet-stream")
    suspend fun uploadBundle(@Body bundle: Bundle): UploadResponse
}