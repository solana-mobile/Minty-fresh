package com.nft.gallery.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.modules.nfts.NftClient
import com.solana.core.PublicKey
import com.solana.core.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

data class MyMint(
    val id: String,
    val mediaUrl: String,
)

@HiltViewModel
class MyMintsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val pubKey = PublicKey("5nmoLTjaCYxDY2iZEAHEnbkTyPRrqtF6mrGwXxuJGr4C")
    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL("https://solana-mainnet.g.alchemy.com/v2/wNKQI1tTf6CBkHRo7fQGlyQxCQVy1pxj")),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )
    private val identityDriver = object : IdentityDriver {
        // fill in, only the publicKey attribute is needed for lookups
        override val publicKey = pubKey

        override fun sendTransaction(
            transaction: Transaction,
            recentBlockHash: String?,
            onComplete: (Result<String>) -> Unit
        ) {
        }

        override fun signAllTransactions(
            transactions: List<Transaction>,
            onComplete: (Result<List<Transaction?>>) -> Unit
        ) {
        }

        override fun signTransaction(
            transaction: Transaction,
            onComplete: (Result<Transaction>) -> Unit
        ) {
        }
    }
    private val nftClient = NftClient(connection, identityDriver)

    private var _viewState: MutableStateFlow<List<MyMint>> = MutableStateFlow(listOf())

    val viewState = _viewState.asStateFlow()

    fun loadMyMints() {
        viewModelScope.launch(Dispatchers.IO) {
            _viewState.update {
                try {
                    nftClient.findAllByOwner(pubKey).getOrThrow()
                        .filterNotNull()
                        .filter { it.collection != null }
                        .map { MyMint(it.mint.toString(), it.uri) }
                } catch (e: Exception) {
                    listOf()
                }
            }
        }
    }
}