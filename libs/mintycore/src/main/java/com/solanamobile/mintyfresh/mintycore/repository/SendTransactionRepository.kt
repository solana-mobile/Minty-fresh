package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.*
import com.solana.core.Transaction
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class SendTransactionRepository @Inject constructor(private val connection: Connection)  {

    suspend fun sendTransaction(transaction: Transaction) =
        connection.sendTransaction(transaction).getOrThrow()

    suspend fun confirmTransaction(transactionSignature: String): Boolean =
        withTimeout(connection.transactionOptions.timeout.toMillis()) {

            val commitment = connection.transactionOptions.commitment.toString()

            suspend fun confirmationStatus() =
                connection.getSignatureStatuses(listOf(transactionSignature), null)
                    .getOrNull()?.first()?.also { it.err?.let { error ->
                        throw Error("Transaction Confirmation Failed: $error")
                    } }

            // wait for desired transaction status
            while(confirmationStatus()?.confirmationStatus == commitment) {

                // wait a bit before retrying
                val millis = System.currentTimeMillis()
                var inc = 0
                while(System.currentTimeMillis() - millis < 300 && isActive) { inc++ }

                if (!isActive) break // breakout after timeout
            }

            return@withTimeout isActive
        }
}