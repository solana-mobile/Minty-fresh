package com.solanamobile.mintyfresh.networkinterface.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import kotlinx.coroutines.flow.Flow

interface IMyMintsUseCase {

    fun getCachedMints(publicKey: PublicKey): Flow<List<MyMint>>

    suspend fun getAllUserMintyFreshNfts(publicKey: PublicKey): List<NFT>
}