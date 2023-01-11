package com.nft.gallery.repository

import com.metaplex.lib.Metaplex
import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import com.solana.core.Transaction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFTMintyRepository @Inject constructor() {

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
    private val metaplex = Metaplex(
        connection, identityDriver, OkHttpSharedStorageDriver(
            OkHttpClient()
        )
    )

    suspend fun getAllNftsFromMinty() = withContext(Dispatchers.IO) {
        nftClient.findAllByOwner(pubKey).getOrThrow()
            .filterNotNull()
            .filter { it.collection != null }
        // TODO better filter to catch only the "Minty" ones. Constant collection naming for instance?
    }

    suspend fun getNftsMetadata(nft: NFT) = withContext(Dispatchers.IO) {
        val response = CompletableDeferred<JsonMetadata>()
        nft.metadata(metaplex) { result ->
            result.onSuccess { metadata ->
                response.complete(metadata)
            }
            result.onFailure {
                response.completeExceptionally(it)
            }
        }
        return@withContext response.await()
    }
}