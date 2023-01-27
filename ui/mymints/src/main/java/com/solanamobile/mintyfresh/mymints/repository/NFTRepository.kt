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
import com.solanamobile.mintyfresh.core.BuildConfig
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

    suspend fun getAllNfts() = withContext(Dispatchers.IO) {
        nftClient.findAllByOwner(publicKey)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun getNftsMetadata(nft: NFT) = withContext(Dispatchers.IO) {
        return@withContext nft.metadata(storageDriver).getOrThrow()
    }
}