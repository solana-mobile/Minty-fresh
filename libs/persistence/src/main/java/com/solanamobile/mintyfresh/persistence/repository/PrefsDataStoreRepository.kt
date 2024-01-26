package com.solanamobile.mintyfresh.persistence.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data")

class PrefsDataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val publicKeyFlow: Flow<String> =
        context.dataStore.data
            .map { preferences ->
                preferences[pubkeyPref] ?: ""
            }

    val accountLabelFlow: Flow<String> =
        context.dataStore.data
            .map { preferences ->
                preferences[accountLabelPref] ?: ""
            }

    val authTokenFlow: Flow<String> =
        context.dataStore.data
            .map { preferences ->
                preferences[authTokenPref] ?: ""
            }

    val walletUriFlow: Flow<String> =
        context.dataStore.data
            .map { preferences ->
                preferences[walletUriPref] ?: ""
            }

    suspend fun updateWalletDetails(pubkey: String, label: String, authToken: String, walletUri: String?) {
        context.dataStore.edit { settings ->
            settings[pubkeyPref] = pubkey
            settings[accountLabelPref] = label
            settings[authTokenPref] = authToken
            walletUri?.run {
                settings[walletUriPref] = this
            } ?: settings.remove(walletUriPref)
        }
    }

    suspend fun clearWalletDetails() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        val pubkeyPref = stringPreferencesKey("public_key")
        val accountLabelPref = stringPreferencesKey("account_label")
        val authTokenPref = stringPreferencesKey("auth_token")
        val walletUriPref = stringPreferencesKey("wallet_uri_base")
    }
}