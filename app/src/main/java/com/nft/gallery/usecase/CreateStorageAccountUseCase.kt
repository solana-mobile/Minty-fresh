package com.nft.gallery.usecase

import com.nft.gallery.BuildConfig
import com.nft.gallery.repository.LatestBlockhashRepository
import com.nft.gallery.repository.ShadowDriveAccountRepository
import com.nft.gallery.viewmodel.iconUri
import com.nft.gallery.viewmodel.identityName
import com.nft.gallery.viewmodel.solanaUri
import com.solana.core.SerializeConfig
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
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
    private val persistenceUseCase: PersistenceUseCase,
    private val storageRepository: ShadowDriveAccountRepository,
    private val blockhashRepository: LatestBlockhashRepository,
    ) {

    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.None)

    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

    suspend fun createStorageAccount(sender: ActivityResultSender, accountName: String, requestedBytes: ULong) {
        withContext(Dispatchers.IO) {
            
            val owner = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.publicKey else null
            }.stateIn(this).value

            val authToken = persistenceUseCase.walletDetails.map {
                if (it is Connected) it.authToken else null
            }.stateIn(this).value

            check(owner != null)

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

            val signedTx = walletAdapter.transact(sender) {

                authToken?.let {
                    reauthorize(solanaUri, iconUri, identityName, authToken)
                } ?: authorize(solanaUri, iconUri, identityName, BuildConfig.RPC_CLUSTER)

                val signingResult = signTransactions(arrayOf(transactionBytes))

                return@transact signingResult.signedPayloads[0]
            }

            _createAccountState.value = CreateAccountState.RequestingAccount(signedTx)

            storageRepository.createStorageAccount(signedTx)

            _createAccountState.value = CreateAccountState.Complete("")
        }
    }
}