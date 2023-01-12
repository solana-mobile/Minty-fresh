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

class NFTMintyRepository(private val publicKey: PublicKey) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL("https://solana-mainnet.g.alchemy.com/v2/wNKQI1tTf6CBkHRo7fQGlyQxCQVy1pxj")),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )
    private val identityDriver = ReadOnlyIdentityDriver(publicKey, connection)
    private val nftClient = NftClient(connection, identityDriver)
    private val metaplex = Metaplex(
        connection, identityDriver, OkHttpSharedStorageDriver(
            OkHttpClient()
        )
    )

    suspend fun getAllNftsFromMinty(collectionName: String) = withContext(Dispatchers.IO) {
        // Getting all the NFTs that have a Collection
        val nftsWithCollection = nftClient.findAllByOwner(publicKey).getOrThrow()
            .filterNotNull()
            .filter { it.collection != null }

        // Finding the collectionName's pubKey (stopping as soon as we find it)
        val iterator = nftsWithCollection.iterator()
        var foundCollection = false
        var collectionPubKey = ""
        while (iterator.hasNext() && !foundCollection) {
            val nft = iterator.next()
            nft.collection?.let { collection ->
                val collectionNft = nftClient.findByMint(collection.key).getOrThrow()
                if (collectionNft.name == collectionName) {
                    collectionPubKey = collection.key.toString()
                    foundCollection = true
                }
            }
        }

        // Filtering the list of NFTs by the collection's pubKey
        return@withContext nftsWithCollection
            .filter { it.collection?.key.toString() == collectionPubKey }
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