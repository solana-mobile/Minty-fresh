package com.nft.gallery.usecase

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.portto.solana.web3.PublicKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

sealed class WalletConnection

object NotConnected : WalletConnection()

data class Connected(
    val publicKey: PublicKey,
    val accountLabel: String,
    val authToken: String
): WalletConnection()

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data")

class PersistenceUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    val EXAMPLE_COUNTER = intPreferencesKey("example_counter")

    val exampleCounterFlow: Flow<Int> =
        context.dataStore.data
            .map { preferences ->
                preferences[EXAMPLE_COUNTER] ?: 0
            }

    suspend fun incrementCounter() {
        context.dataStore.edit { settings ->
            val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
            settings[EXAMPLE_COUNTER] = currentCounterValue + 1
        }
    }

    private var connection: WalletConnection = NotConnected

    fun getWalletConnection(): WalletConnection {
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

    fun persistConnection(pubKey: PublicKey, accountLabel: String, token: String) {
        sharedPreferences.edit().apply {
            putString(PUBKEY_KEY, pubKey.toBase58())
            putString(ACCOUNT_LABEL, accountLabel)
            putString(AUTH_TOKEN_KEY, token)
        }.apply()

        connection = Connected(pubKey, accountLabel, token)
    }

    fun clearConnection() {
        sharedPreferences.edit().apply {
            putString(PUBKEY_KEY, "")
            putString(ACCOUNT_LABEL, "")
            putString(AUTH_TOKEN_KEY, "")
        }.apply()

        connection = NotConnected
    }

    companion object {
        const val PUBKEY_KEY = "stored_pubkey"
        const val ACCOUNT_LABEL = "stored_account_label"
        const val AUTH_TOKEN_KEY = "stored_auth_token"
    }

}