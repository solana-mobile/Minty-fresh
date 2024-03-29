package com.solanamobile.mintyfresh.persistence.usecase

import android.net.Uri
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.persistence.repository.PrefsDataStoreRepository
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

sealed class UserWalletDetails

object NotConnected : UserWalletDetails()

data class Connected(
    val publicKey: String,
    val accountLabel: String,
    val authToken: String,
    val walletUriBase: String? = null
): UserWalletDetails()

/**
 * Saves & clears an MWA connection to an on-device wallet, with convenience value providing
 * connection status
 */
class WalletConnectionUseCase @Inject constructor(
    private val dataStoreRepository: PrefsDataStoreRepository,
) {

    val walletDetails = combine(
        dataStoreRepository.publicKeyFlow,
        dataStoreRepository.accountLabelFlow,
        dataStoreRepository.authTokenFlow,
        dataStoreRepository.walletUriFlow)
    { pubKey, label, authToken, walletUriBase ->
        if (pubKey.isEmpty() || label.isEmpty() || authToken.isEmpty()) {
            NotConnected
        } else {
            Connected(
                publicKey = pubKey,
                accountLabel = label,
                authToken = authToken,
                walletUriBase = walletUriBase
            )
        }
    }

    suspend fun persistConnection(pubKey: ByteArray, accountLabel: String, token: String, walletUri: Uri?) {
        persistConnection(PublicKey(pubKey), accountLabel, token, walletUri.toString())
    }

    suspend fun persistConnection(pubKey: String, accountLabel: String, token: String, walletUri: Uri?) {
        persistConnection(PublicKey.valueOf(pubKey), accountLabel, token, walletUri.toString())
    }

    private suspend fun persistConnection(pubKey: PublicKey, accountLabel: String, token: String, walletUri: String?) {
        dataStoreRepository.updateWalletDetails(pubKey.toBase58(), accountLabel, token, walletUri)
    }

    suspend fun clearConnection() {
        dataStoreRepository.clearWalletDetails()
    }

}