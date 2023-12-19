package com.solanamobile.mintyfresh.mintycore.endpoints

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.Path

interface ArweaveEndpoints {
    @HEAD("{ID}")
    suspend fun exists(@Path("ID") transactionId: String): Response<Void>
    @GET("{ID}")
    suspend fun getTransactionData(@Path("ID") transactionId: String): String
}