/*
 * GetLatestBlockhashUseCase
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.usecase

import com.metaplex.lib.drivers.solana.*
import javax.inject.Inject

class GetLatestBlockhashUseCase @Inject constructor(private val connectionDriver: Connection) {
    suspend fun getlatestBlockHash() = connectionDriver.getRecentBlockhash().getOrThrow()
}