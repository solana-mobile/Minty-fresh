package com.solanamobile.mintyfresh.mymints.repository

import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.models.NFT
import com.metaplex.lib.modules.token.models.metadata
import com.solana.core.PublicKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class NFTRepository @AssistedInject constructor(
    @Assisted private val publicKey: PublicKey,
    connection: Connection
) {

    @AssistedFactory
    interface NFTRepositoryFactory {
        fun create(publicKey: PublicKey): NFTRepository
    }

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