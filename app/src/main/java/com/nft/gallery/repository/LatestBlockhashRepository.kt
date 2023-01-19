/*
 * LatestBlockhashRepository
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.getRecentBlockhash
import javax.inject.Inject

class LatestBlockhashRepository @Inject constructor(private val connectionDriver: Connection) {
    suspend fun getLatestBlockHash() = connectionDriver.getRecentBlockhash().getOrThrow()
}