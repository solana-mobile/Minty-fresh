package com.nft.gallery.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.experimental.jen.tokenmetadata.TokenMetadataInstructions
import com.metaplex.lib.extensions.confirmTransaction
import com.metaplex.lib.extensions.signSendAndConfirm
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.metaplex.lib.programs.token_metadata.MasterEditionAccount
import com.metaplex.lib.programs.token_metadata.accounts.MetadataAccount
import com.nft.gallery.BuildConfig
import com.nft.gallery.constant.mintyFreshCollectionName
import com.nft.gallery.metaplex.MetaplexHttpDriver
import com.nft.gallery.metaplex.MobileWalletIdentityWrapper
import com.nft.gallery.repository.MetadataUploadRepository
import com.nft.gallery.repository.StorageUploadRepository
import com.solana.core.HotAccount
import com.solana.core.PublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import javax.inject.Inject
import kotlin.math.pow

enum class MintState {
    NONE, UPLOADING_FILE, CREATING_METADATA, MINTING, COMPLETE
}

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintingInProgress: Boolean = false,
    val mintState: MintState = MintState.NONE
)
val rpcUrl = BuildConfig.SOLANA_RPC_URL

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application,
    private val storageRepository: StorageUploadRepository,
    private val metadataRepository: MetadataUploadRepository
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(PerformMintViewState())

    val viewState: StateFlow<PerformMintViewState> = _viewState.asStateFlow()

    /**
     * We should perhaps think about updating the ViewState with form input, then it wouldn't
     * have to be passed here. Also we'll want to support dynamic attributes in the future.
     */
    fun performMint(sender: ActivityResultSender, title: String, desc: String, imgUrl: String) {
        viewModelScope.launch {

            _viewState.update {
                _viewState.value.copy(mintState = MintState.UPLOADING_FILE)
            }

            val nftImageUrl = storageRepository.uploadFile(imgUrl)

            _viewState.update {
                _viewState.value.copy(mintState = MintState.CREATING_METADATA)
            }
            val metadataUrl = metadataRepository.uploadMetadata(title, desc, nftImageUrl)

            _viewState.update {
                _viewState.value.copy(
                    mintingInProgress = true,
                    mintState = MintState.MINTING
                )
            }

            MobileWalletAdapter().apply {
                transact(sender) {
                    val auth = authorize(solanaUri, iconUri, identityName, RpcCluster.Devnet)

                    val connection = SolanaConnectionDriver(
                        MetaplexHttpDriver(rpcUrl),
                        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
                    )

                    val identityDriver = object : MobileWalletIdentityWrapper() {
                        override val publicKeyBytes: ByteArray
                            get() = auth.publicKey

                        override fun mobileWalletSignTransaction(transaction: ByteArray,
                                                                 onComplete: (ByteArray) -> Unit) {
                            viewModelScope.launch {
                                val result = signTransactions(arrayOf(transaction))
                                onComplete(result.signedPayloads[0])
                            }
                        }
                    }

                    withContext(Dispatchers.IO) {

                        val client = NftClient(connection, identityDriver)

                        val existingCollection =
                            client.findAllByOwner(identityDriver.publicKey).getOrThrow().run {
                                find {
                                    it?.name == mintyFreshCollectionName && it.collection == null
                                }
                            }

                        val collection: PublicKey = if (existingCollection == null) {
                            val collection = HotAccount()
                            val collectionMetadata = Metadata(
                                name = mintyFreshCollectionName,
                                uri = "",
                                sellerFeeBasisPoints = 0
                            )

                            CreateNftTransactionBuilder(
                                collection.publicKey,
                                collectionMetadata,
                                payer = identityDriver.publicKey,
                                connection = connection
                            ).build().getOrThrow()
                                .signSendAndConfirm(connection, identityDriver, listOf(collection))

                            collection.publicKey
                        } else existingCollection.mint

                        val newMintAccount = HotAccount()
                        val metadata = Metadata(
                            name = title,
                            uri = metadataUrl,
                            sellerFeeBasisPoints = 0,
                            creators = listOf(
                                Creator(identityDriver.publicKey, true, 100.toUByte())
                            ),
                            collection = collection
                        )

                        CreateNftTransactionBuilder(
                            newMintAccount.publicKey,
                            metadata,
                            payer = identityDriver.publicKey,
                            connection = connection
                        ).build().getOrThrow()
                            .addInstruction(TokenMetadataInstructions.VerifyCollection(
                                    metadata = MetadataAccount.pda(newMintAccount.publicKey).getOrThrows(),
                                    collectionAuthority = identityDriver.publicKey,
                                    payer = identityDriver.publicKey,
                                    collectionMint = collection,
                                    collection = MetadataAccount.pda(collection).getOrThrows(),
                                    collectionMasterEditionAccount = MasterEditionAccount.pda(collection).getOrThrows()
                            ))
                            .signSendAndConfirm(connection, identityDriver, listOf(newMintAccount))

//                        connection.airdrop(identityDriver.publicKey, 1f)
//                        val nft = client.create(metadata).getOrThrow()
                        val actualNft = client.findByMint(newMintAccount.publicKey).getOrNull()

                        println("++++++ the nft: ${actualNft?.name}")
                        println("++++++          ${actualNft?.uri}")
                        println("++++++          ${actualNft?.mint}")

                        _viewState.update {
                            _viewState.value.copy(
                                mintingInProgress = false,
                                mintState = MintState.COMPLETE
                            )
                        }
                    }
                }
            }
        }
    }

    interface StartActivityForResultSender {
        fun startActivityForResult(intent: Intent, onActivityCompleteCallback: () -> Unit) // throws ActivityNotFoundException
    }

}

class AirdropRequest(wallet: PublicKey, lamports: Long, commitment: String = "confirmed") : RpcRequest() {

    constructor(wallet: PublicKey, amountSol: Float, commitment: String = "confirmed")
            : this(wallet, (amountSol*10f.pow(9)).toLong(), commitment)

    override val method: String = "requestAirdrop"
    override val params: JsonElement = buildJsonArray {
        add(wallet.toBase58())
        add(lamports)
        addJsonObject {
            put("commitment", commitment)
        }
    }
}

suspend fun Connection.airdrop(wallet: PublicKey, amountSol: Float) =
    get(AirdropRequest(wallet, amountSol), String.serializer()).confirmTransaction(this)