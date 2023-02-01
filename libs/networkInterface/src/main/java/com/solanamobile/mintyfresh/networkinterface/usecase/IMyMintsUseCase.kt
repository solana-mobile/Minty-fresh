package com.solanamobile.mintyfresh.networkinterface.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import kotlinx.coroutines.flow.Flow

interface IMyMintsUseCase<T> {

    fun getCachedMints(publicKey: PublicKey): Flow<List<T>>

    suspend fun getAllUserMintyFreshNfts(publicKey: PublicKey): List<NFT>
}