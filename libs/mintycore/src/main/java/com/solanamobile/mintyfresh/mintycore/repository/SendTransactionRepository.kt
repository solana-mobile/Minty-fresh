package com.solanamobile.mintyfresh.mintycore.repository

import android.content.Context
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.sendTransaction
import com.solana.core.Transaction
import com.solanamobile.mintyfresh.mintycore.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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

            val requiredCommitment = connectionDriver.transactionOptions.commitment.ordinal

            suspend fun confirmationStatus() =
                connectionDriver.getSignatureStatuses(listOf(transactionSignature), null)
                    .getOrNull()?.first()?.also {
                        it.err?.let { error ->
                            throw Error("${context.getString(R.string.transaction_failure_message)}\n$error")
                        }
                    }

            // wait for desired transaction status
            var inc = 1L
            while (true) {
                val currentStatus = confirmationStatus()?.confirmationStatus
                val confirmationEnum =
                    Commitment.values().firstOrNull { it.name.equals(currentStatus, true) }
                val confirmationOrdinal = confirmationEnum?.ordinal ?: -1

                if (confirmationOrdinal >= requiredCommitment) {
                    return@withTimeout Result.success(true)
                } else {
                    // Exponential delay before retrying.
                    delay(500 * inc)
                }
                // breakout after timeout
                if (!isActive) break
                inc++
            }

            return@withTimeout Result.success(isActive)
        }
}