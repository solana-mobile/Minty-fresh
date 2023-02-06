package com.solanamobile.mintyfresh.mintycore.usecase

import android.net.Uri
import android.util.Log
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.solana.core.*
import com.solana.mobilewalletadapter.clientlib.*
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import com.solanamobile.mintyfresh.mintycore.metaplex.jen.shadowdrive.BYTES_PER_GIB
import com.solanamobile.mintyfresh.mintycore.metaplex.jen.shadowdrive.MIN_ACCOUNT_SIZE
import com.solanamobile.mintyfresh.mintycore.repository.*
import com.solanamobile.mintyfresh.persistence.usecase.Connected
import com.solanamobile.mintyfresh.persistence.usecase.WalletConnectionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bitcoinj.core.Base58
import java.io.File
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.fileSize

sealed interface MintState {
    object None : MintState
    object UploadingMedia : MintState
    object CreatingMetadata : MintState
    object BuildingTransaction : MintState
    object AwaitingConfirmation : MintState
    class Signing(val transaction: ByteArray) : MintState
    class Minting(val mintAddress: PublicKey) : MintState
    class Complete(val transactionSignature: String) : MintState
    class Error(val message: String): MintState
}

class PerformMintUseCase @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val shadowDriveRepository: ShadowDriveAccountRepository,
    private val persistenceUseCase: WalletConnectionUseCase,
    private val mintTransactionRepository: MintTransactionRepository,
    private val blockhashRepository: LatestBlockhashRepository,
    private val sendTransactionRepository: SendTransactionRepository
) {

    private val _mintState = MutableStateFlow<MintState>(MintState.None)

    val mintState: StateFlow<MintState> = _mintState

    suspend fun performMint(identityUri: Uri,
                            iconUri: Uri,
                            identityName: String,
                            sender: ActivityResultSender,
                            title: String,
                            desc: String,
                            filePath: String
    ) = withContext(Dispatchers.IO) {
        var authToken = persistenceUseCase.walletDetails.map {
            if (it is Connected) it.authToken else null
        }.stateIn(this).value

        val walletAddy = persistenceUseCase.walletDetails.map {
            if (it is Connected) it.publicKey else null
        }.stateIn(this).value

        check(walletAddy != null)

        val creator = PublicKey(walletAddy)

        //region Storage Account Creation
        // begin building the storage account transaction
        _mintState.value = MintState.BuildingTransaction

        val fileSizeBytes = Path(filePath).fileSize()
        val estimatedMetadataBytes = 500 * 3
        val fileSizeGb = fileSizeBytes.toFloat() / BYTES_PER_GIB.toLong()

        val shadesPerGb = shadowDriveRepository.getConfigurationInfo().getOrThrow().data?.shadesPerGib

        // adding a bit of extra storage here to see if this fixes my issue
        val accountSize = (fileSizeBytes*2 + estimatedMetadataBytes).toULong()

        val createAccountTx = shadowDriveRepository.buildCreateStorageAccountTransaction(
            "$title NFT Storage Account",
            accountSize,
            creator
        )

        createAccountTx.setRecentBlockHash(blockhashRepository.getLatestBlockHash())

        val accountTransactionBytes =
            createAccountTx.serialize(
                SerializeConfig(
                    requireAllSignatures = false,
                    verifySignatures = false
                )
            )

        // begin signing transaction step
        _mintState.value = MintState.Signing(accountTransactionBytes)
        delay(700)

        val partialSignedTx = walletAdapter.transact(sender) {
            authToken = (authToken?.let {
                reauthorize(identityUri, iconUri, identityName, it)
            } ?: authorize(identityUri, iconUri, identityName, RpcCluster.MainnetBeta)).authToken  //TODO: cluster from networking layer

            val signingResult = signTransactions(arrayOf(accountTransactionBytes))

            return@transact signingResult.signedPayloads[0]
        }

        // await storage account creation
        _mintState.value = MintState.AwaitingConfirmation

        // send partially signed transaction to shadow drive
        val createAccountResponse = shadowDriveRepository.createStorageAccount(partialSignedTx.successPayload!!)

        // await confirmation on the account creation transaction
        sendTransactionRepository.confirmTransaction(createAccountResponse.transitionSignature)

        // save the newly created storage account address for upload
        val storageAccount = createAccountResponse.shadowBucket
        //endregion

        //region Upload Media Files
        _mintState.value = MintState.CreatingMetadata

        val imageUrl = "https://shdw-drive.genesysgo.net/${storageAccount}/$title-media.png"
        val json = Json.encodeToString(
            JsonMetadata(
                name = title,
                description = desc,
                image = imageUrl,
                attributes = listOf(
                    JsonMetadata.Attribute("Minty Fresh", "true")
                ),
                properties = JsonMetadata.Properties(
                    files = listOf(
                        JsonMetadata.Properties.File(imageUrl, "image/png")
                    ),
                    category = "image"
                )
            )
        )

        val fileNameHash = MessageDigest.getInstance("SHA-256")
        val fileNames = listOf("$title-media", title)

        fileNameHash.update(fileNames.joinToString().toByteArray())

        val message = "Shadow Drive Signed Message:\nStorage Account: ${storageAccount}\n" +
                "Upload files with hash: ${fileNameHash.digest().fold("") { a, c -> a + "%02x".format(c) }}"

        _mintState.value = MintState.Signing(message.toByteArray())
        delay(700)

        // sign the file upload message
        val signedMessage = walletAdapter.transact(sender) {
            authToken = (authToken?.let {
                reauthorize(identityUri, iconUri, identityName, it)
            } ?: authorize(identityUri, iconUri, identityName, RpcCluster.MainnetBeta)).authToken  //TODO: cluster from networking layer

            val signingResult = signMessages(arrayOf(message.toByteArray()), arrayOf(creator.pubkey))

            return@transact signingResult.signedPayloads[0]
        }.successPayload

        // upload the media file
        _mintState.value = MintState.UploadingMedia

        // TODO: should check the response for file upload location, errors etc.
        val uploadResponse = shadowDriveRepository.uploadNftMedia(
            creator,
            PublicKey(storageAccount),
            title,
            File(filePath),
            json,
            Base58.encode(signedMessage)
        )
        Log.d("SHADOW DRIVE", "uploaded response: $uploadResponse")

        uploadResponse.finalizedLocations.forEach {
            Log.d("SHADOW DRIVE", "uploaded file to: $it")
        }
        //endregion

        //region Mint
        // begin building the nft mint transaction
        _mintState.value = MintState.BuildingTransaction

        val mintAccount = HotAccount()
        val metadataUrl = "https://shdw-drive.genesysgo.net/${storageAccount}/$title"
        val mintTxn = mintTransactionRepository.buildMintTransaction(title, metadataUrl, mintAccount.publicKey, creator)

        mintTxn.setRecentBlockHash(blockhashRepository.getLatestBlockHash())

        val transactionBytes =
            mintTxn.serialize(SerializeConfig(
                requireAllSignatures = false,
                verifySignatures = false
            ))

        // begin signing transaction step
        _mintState.value = MintState.Signing(transactionBytes)

        val txResult = walletAdapter.transact(sender) {
            authToken = (authToken?.let {
                reauthorize(identityUri, iconUri, identityName, it)
            } ?: authorize(identityUri, iconUri, identityName, RpcCluster.MainnetBeta)).authToken  //TODO: cluster from networking layer

            val signingResult = signTransactions(arrayOf(transactionBytes))

            return@transact signingResult.signedPayloads[0].sliceArray(1 until 1 + SIGNATURE_LENGTH)
        }

        when (txResult) {
            is TransactionResult.Success -> {
                txResult.successPayload?.let { primarySignature ->
                    // rebuild transaction object from signed bytes
                    // there is a deserialization bug in solana.core.Message.from(byteArray) so have to
                    // build up the Message (and Transaction) object manually (for now)
                    // val signed = Transaction.from(signedBytes)
                    val signed = Transaction().apply {
                        setRecentBlockHash(mintTxn.recentBlockhash)
                        feePayer = creator
                        addInstruction(*mintTxn.instructions.toTypedArray())
                        addSignature(creator, primarySignature)
                    }

                    // now that the primary signer (creator) has signed, the mint account can sign
                    signed.partialSign(mintAccount)

                    _mintState.value = MintState.Minting(mintAccount.publicKey)

                    // send the signed transaction to the cluster
                    val transactionSignature = sendTransactionRepository.sendTransaction(signed)

                    _mintState.value = MintState.AwaitingConfirmation

                    // Await for transaction confirmation
                    sendTransactionRepository.confirmTransaction(transactionSignature)

                    _mintState.value = MintState.Complete(transactionSignature)
                }
            }
            is TransactionResult.Failure -> {
                _mintState.value = MintState.Complete(txResult.message)
            }
            else -> { }
        }
        //endregion
    }
}