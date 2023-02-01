package com.solanamobile.mintyfresh.networkinterfaceimpl.repository

import com.metaplex.lib.modules.nfts.models.NFT
import com.metaplex.lib.modules.token.models.metadata
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.networkinterfaceimpl.pda.mintyFreshCreatorPda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * TODO: This class will be part of the networking layer in the final PR. This class - a repository
 * that gets 1.) all my mints 2.) metadata - is absolutely one of the classes that can have
 * multiple implementations. For the public implementation, it can look more or less like this. For the
 * prod implementation, it can be communicating with our API abstraction.
 */
class NFTRepository @Inject constructor(
    private val nftInfraFactory: NftInfraFactory
) {

    suspend fun getAllUserMintyFreshNfts(publicKey: PublicKey): List<NFT> =
        getAllNfts(publicKey).filter { allUserNFts ->
            allUserNFts.creators.firstOrNull { nft -> nft.address == publicKey } != null &&
            allUserNFts.creators.firstOrNull { nft -> nft.address == mintyFreshCreatorPda } != null
        }

    private suspend fun getAllNfts(publicKey: PublicKey) = withContext(Dispatchers.IO) {
        val client = nftInfraFactory.createNftClient(publicKey)

        client.findAllByOwner(publicKey)
            .getOrThrow()
            .filterNotNull()
    }

    suspend fun getNftsMetadata(nft: NFT) = withContext(Dispatchers.IO) {
        return@withContext nft.metadata(nftInfraFactory.storageDriver).getOrThrow()
    }
}