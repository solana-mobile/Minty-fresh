package com.solanamobile.mintyfresh.mymints.repository

import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.NFT
import com.metaplex.lib.modules.token.models.metadata
import com.solana.core.PublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.URL

/**
 * TODO: This class will be part of the networking layer in the final PR. This class - a repository
 * that gets 1.) all my mints 2.) metadata - is absolutely one of the classes that can have
 * multiple implementations. For the public implementation, it can look more or less like this. For the
 * prod implementation, it can be communicating with our API abstraction.
 */
class NFTRepository(private val publicKey: PublicKey) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL("https://api.devnet.solana.com")),  //TODO: Figure out how to get correct value in public impl of networking layer
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )

    private val identityDriver = ReadOnlyIdentityDriver(publicKey, connection)
    private val storageDriver = OkHttpSharedStorageDriver(OkHttpClient())
    private val nftClient = NftClient(connection, identityDriver)

    suspend fun getAllNfts() = withContext(Dispatchers.IO) {
        nftClient.findAllByOwner(publicKey)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun getNftsMetadata(nft: NFT) = withContext(Dispatchers.IO) {
        return@withContext nft.metadata(storageDriver).getOrThrow()
    }
}