package com.solanamobile.mintyfresh.mintycore.repository

import android.content.Context
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.sendTransaction
import com.solana.core.Transaction
import com.solanamobile.mintyfresh.mintycore.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class SendTransactionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectionDriver: Connection
) {
    suspend fun sendTransaction(transaction: Transaction) =
        connectionDriver.sendTransaction(transaction)

    suspend fun confirmTransaction(transactionSignature: String): Result<Boolean> =
        withTimeout(connectionDriver.transactionOptions.timeout.toMillis()) {

            val commitment = connectionDriver.transactionOptions.commitment.toString()

            suspend fun confirmationStatus() =
                connectionDriver.getSignatureStatuses(listOf(transactionSignature), null)
                    .getOrNull()?.first()?.also {
                        it.err?.let { error ->
                            Result.failure<Boolean>(Error("${context.getString(R.string.transaction_failure_message)}\n$error"))
                        }
                    }

            // wait for desired transaction status
            while (confirmationStatus()?.confirmationStatus == commitment) {

                // wait a bit before retrying
                val millis = System.currentTimeMillis()
                var inc = 0
                while (System.currentTimeMillis() - millis < 300 && isActive) {
                    inc++
                }

                if (!isActive) break // breakout after timeout
            }

            return@withTimeout Result.success(isActive)
        }
}