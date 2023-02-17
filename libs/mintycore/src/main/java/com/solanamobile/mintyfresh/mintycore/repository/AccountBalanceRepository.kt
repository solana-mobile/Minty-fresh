package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.Connection
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.metaplex.getAccountBalance
import javax.inject.Inject

class AccountBalanceRepository @Inject constructor(private val connection: Connection) {
    suspend fun getSolBalanceForAccount(accountAddress: PublicKey): Result<Long> =
        connection.getAccountBalance(accountAddress).map { it ?: -1 }
}