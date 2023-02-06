package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.getRecentBlockhash
import javax.inject.Inject

class LatestBlockhashRepository @Inject constructor(private val connection: Connection) {
    suspend fun getLatestBlockHash() = connection.getRecentBlockhash().getOrThrow()
}