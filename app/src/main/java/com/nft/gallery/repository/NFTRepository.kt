package com.nft.gallery.repository

import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.NFT
import com.metaplex.lib.modules.token.models.metadata
import com.nft.gallery.BuildConfig
import com.nft.gallery.metaplex.MintyFreshCreatorPda
import com.solana.core.PublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.URL

class NFTRepository(private val publicKey: PublicKey) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL(BuildConfig.SOLANA_RPC_URL)),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )

    private val identityDriver = ReadOnlyIdentityDriver(publicKey, connection)
    private val storageDriver = OkHttpSharedStorageDriver(OkHttpClient())
    private val nftClient = NftClient(connection, identityDriver)

    private val mintyFreshCreatorPda = MintyFreshCreatorPda(publicKey)

    suspend fun getAllMintyFreshNfts() = withContext(Dispatchers.IO) {
        nftClient.findAllByCreator(mintyFreshCreatorPda, 2)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun getAllNfts() = withContext(Dispatchers.IO) {
        nftClient.findAllByOwner(publicKey)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun findByMint(publicKey: PublicKey) = withContext(Dispatchers.IO) {
        nftClient.findByMint(publicKey).getOrThrow()
    }

    suspend fun getNftsMetadata(nft: NFT) = withContext(Dispatchers.IO) {
        return@withContext nft.metadata(storageDriver).getOrThrow()
    }
}