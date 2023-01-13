package com.nft.gallery.repository

import com.metaplex.lib.Metaplex
import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.URL

class NFTRepository(private val publicKey: PublicKey) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL("https://api.devnet.solana.com")),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )
    private val identityDriver = ReadOnlyIdentityDriver(publicKey, connection)
    private val nftClient = NftClient(connection, identityDriver)
    private val metaplex = Metaplex(
        connection, identityDriver, OkHttpSharedStorageDriver(
            OkHttpClient()
        )
    )

    suspend fun getAllNfts() = withContext(Dispatchers.IO) {
        nftClient.findAllByOwner(publicKey)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun findByMint(publicKey: PublicKey) = withContext(Dispatchers.IO) {
        nftClient.findByMint(publicKey).getOrThrow()
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