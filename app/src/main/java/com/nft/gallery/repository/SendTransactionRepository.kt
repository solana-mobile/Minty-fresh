/*
 * SendTransactionUseCase
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.sendTransaction
import com.solana.core.Transaction
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class SendTransactionRepository @Inject constructor(private val connectionDriver: Connection)  {
    suspend fun sendTransaction(transaction: Transaction) =
        connectionDriver.sendTransaction(transaction).getOrThrow()

    suspend fun confirmTransaction(transactionSignature: String): Boolean =
        withTimeout(connectionDriver.transactionOptions.timeout.toMillis()) {

            val commitment = connectionDriver.transactionOptions.commitment.toString()

            suspend fun confirmationStatus() =
                connectionDriver.getSignatureStatuses(listOf(transactionSignature), null)
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