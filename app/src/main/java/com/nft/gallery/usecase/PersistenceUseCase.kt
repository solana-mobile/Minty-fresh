package com.nft.gallery.usecase

import android.content.SharedPreferences
import com.nft.gallery.repository.PrefsDataStoreRepository
import com.portto.solana.web3.PublicKey
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
    private val sharedPreferences: SharedPreferences
) {

    private var connection: UserWalletDetails = NotConnected

    fun getWalletConnection(): UserWalletDetails {
        return when(connection) {
            is Connected -> connection
            is NotConnected -> {
                val key = sharedPreferences.getString(PUBKEY_KEY, "")
                val accountLabel = sharedPreferences.getString(ACCOUNT_LABEL, "") ?: ""
                val token = sharedPreferences.getString(AUTH_TOKEN_KEY, "")

                val newConn = if (key.isNullOrEmpty() || token.isNullOrEmpty()) {
                    NotConnected
                } else {
                    Connected(PublicKey(key), accountLabel, token)
                }

                return newConn
            }
        }
    }

    suspend fun persistConnection(pubKey: PublicKey, accountLabel: String, token: String) {
        dataStoreRepository.updateWalletDetails(pubKey.toBase58(), accountLabel, token)

        connection = Connected(pubKey, accountLabel, token)
    }

    suspend fun clearConnection() {
        dataStoreRepository.clearWalletDetails()

        connection = NotConnected
    }

    companion object {
        const val PUBKEY_KEY = "stored_pubkey"
        const val ACCOUNT_LABEL = "stored_account_label"
        const val AUTH_TOKEN_KEY = "stored_auth_token"
    }

}