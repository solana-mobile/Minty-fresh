package com.solanamobile.mintyfresh.mintycore.usecase

import android.net.Uri
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.repository.ShadowDriveAccountRepository
import com.solana.core.SerializeConfig
import com.solana.mobilewalletadapter.clientlib.*
import com.solanamobile.mintyfresh.mintycore.repository.LatestBlockhashRepository
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface CreateAccountState {
    object None : CreateAccountState
    object BuildingTransaction : CreateAccountState
    class Signing(val transaction: ByteArray) : CreateAccountState
    class RequestingAccount(val signedTransaction: ByteArray) : CreateAccountState
    class Complete(val transactionSignature: String) : CreateAccountState
}

class CreateStorageAccountUseCase @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: WalletConnectionUseCase,
    private val storageRepository: ShadowDriveAccountRepository,
    private val blockhashRepository: LatestBlockhashRepository,
) {

    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.None)

    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

    suspend fun createStorageAccount(identityUri: Uri,
                                     iconUri: Uri,
                                     identityName: String,
                                     sender: ActivityResultSender,
                                     accountName: String,
                                     requestedBytes: ULong) {
        withContext(Dispatchers.IO) {
            
            val walletAddy = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.publicKey else null
            }.stateIn(this).value

            val authToken = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.authToken else null
            }.stateIn(this).value

            check(walletAddy != null)

            val owner = PublicKey(walletAddy)

            _createAccountState.value = CreateAccountState.BuildingTransaction

            val csaTxn = storageRepository.buildCreateStorageAccountTransaction(accountName, requestedBytes, owner)

            csaTxn.setRecentBlockHash(blockhashRepository.getLatestBlockHash())
            csaTxn.feePayer = owner

            val transactionBytes = csaTxn.serialize(
                SerializeConfig(
                    requireAllSignatures = false,
                    verifySignatures = false
                )
            )

            // begin signing transaction step
            _createAccountState.value = CreateAccountState.Signing(transactionBytes)
            delay(700)

            val txResult = walletAdapter.transact(sender) {

                authToken?.let {
                    reauthorize(identityUri, iconUri, identityName, authToken)
                } ?: authorize(identityUri, iconUri, identityName, RpcCluster.Devnet)  //TODO: cluster from networking layer

                val signingResult = signTransactions(arrayOf(transactionBytes))

                return@transact signingResult.signedPayloads[0]
            }

            when (txResult) {
                is TransactionResult.Success -> {
                    txResult.successPayload?.let { signedTx ->
                        _createAccountState.value = CreateAccountState.RequestingAccount(signedTx)

                        storageRepository.createStorageAccount(signedTx)

                        _createAccountState.value = CreateAccountState.Complete("")
                    }
                }
                is TransactionResult.Failure -> {
                    _createAccountState.value = CreateAccountState.Complete("Failure") // TODO: handle failure case
                }
                else -> { }
            }
        }
    }
}