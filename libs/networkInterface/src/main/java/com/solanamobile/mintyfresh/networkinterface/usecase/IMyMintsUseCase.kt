package com.solanamobile.mintyfresh.networkinterface.usecase

import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import kotlinx.coroutines.flow.Flow

/**
 * MyMints uscase interface.
 */
interface IMyMintsUseCase {

    /**
     * Returns a flow of cached mints.
     */
    fun getCachedMints(publicKey: String): Flow<List<MyMint>>

    /**
     * Returns a list of all mints using this app for a given [publicKey]
     */
    suspend fun getAllUserMintyFreshNfts(publicKey: String): List<MyMint>
}