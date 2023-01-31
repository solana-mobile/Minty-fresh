package com.solanamobile.mintyfresh.core.peristence.usecase

import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.persistence.repository.PrefsDataStoreRepository
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

sealed class UserWalletDetails

object NotConnected : UserWalletDetails()

data class Connected(
    val publicKey: PublicKey,
    val accountLabel: String,
    val authToken: String
): UserWalletDetails()

class PersistenceUseCase @Inject constructor(
    private val dataStoreRepository: PrefsDataStoreRepository,
) {

    val walletDetails = combine(
        dataStoreRepository.publicKeyFlow,
        dataStoreRepository.accountLabelFlow,
        dataStoreRepository.authTokenFlow)
    { pubKey, label, authToken ->
        if (pubKey.isEmpty() || label.isEmpty() || authToken.isEmpty()) {
            NotConnected
        } else {
            Connected(
                publicKey = PublicKey(pubKey),
                accountLabel = label,
                authToken = authToken
            )
        }
    }

    suspend fun persistConnection(pubKey: PublicKey, accountLabel: String, token: String) {
        dataStoreRepository.updateWalletDetails(pubKey.toBase58(), accountLabel, token)
    }

    suspend fun clearConnection() {
        dataStoreRepository.clearWalletDetails()
    }

}