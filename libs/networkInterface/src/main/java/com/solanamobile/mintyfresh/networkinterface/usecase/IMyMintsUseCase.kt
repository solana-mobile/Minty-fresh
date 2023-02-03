package com.solanamobile.mintyfresh.networkinterface.usecase

import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import kotlinx.coroutines.flow.Flow

interface IMyMintsUseCase {

    fun getCachedMints(publicKey: String): Flow<List<MyMint>>

    suspend fun getAllUserMintyFreshNfts(publicKey: String): List<MyMint>
}