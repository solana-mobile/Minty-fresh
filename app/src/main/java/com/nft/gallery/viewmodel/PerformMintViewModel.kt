package com.nft.gallery.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.Metadata
import com.nft.gallery.BuildConfig
import com.nft.gallery.metaplex.MetaplexHttpDriver
import com.nft.gallery.metaplex.MobileWalletIdentityWrapper
import com.nft.gallery.repository.StorageUploadRepository
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PerformMintViewState(
    val isWalletConnected: Boolean = false,
    val mintingInProgress: Boolean = true
)

val solanaUri = Uri.parse("https://solana.com")
val iconUri = Uri.parse("favicon.ico")
val identityName = "Solana"

val rpcUrl = BuildConfig.SOLANA_RPC_URL

@HiltViewModel
class PerformMintViewModel @Inject constructor(
    application: Application,
    private val storageRepository: StorageUploadRepository
) : AndroidViewModel(application) {

    private var _viewState: MutableStateFlow<PerformMintViewState> = MutableStateFlow(PerformMintViewState())

    val viewState: StateFlow<PerformMintViewState> = _viewState.asStateFlow()

    /**
     * We should perhaps think about updating the ViewState with form input, then it wouldn't
     * have to be passed here. Also we'll want to support dynamic attributes in the future.
     */
    fun performMint(sender: ActivityResultSender, title: String, desc: String, imgUrl: String) {
        viewModelScope.launch {

//            val finalUrl = storageRepository.uploadFile(imgUrl)

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

                        val metadata = Metadata(
                            name = title,
                            // using the actual link from NFT.storage results in a signing error
                            // so using dummy url for now so everything "works"
                            uri = "http://example.com/sd8756fsuyvvbf37684",
                            sellerFeeBasisPoints = 250
                        )

//                        connection.airdrop(identityDriver.publicKey, 1f)
                        val nft = client.create(metadata).getOrThrow()
                        val actualNft = client.findByMint(nft.mint).getOrNull()

                        println("++++++ the nft: ${actualNft?.name}")
                        println("++++++          ${actualNft?.uri}")
                        println("++++++          ${actualNft?.mint}")

                    }
                }
            }
        }
    }

}