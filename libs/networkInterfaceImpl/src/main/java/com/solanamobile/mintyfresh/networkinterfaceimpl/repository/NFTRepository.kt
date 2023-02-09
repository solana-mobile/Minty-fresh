package com.solanamobile.mintyfresh.networkinterfaceimpl.repository

import com.metaplex.lib.modules.nfts.models.NFT
import com.metaplex.lib.modules.token.models.metadata
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.networkinterface.pda.mintyFreshCreatorPubKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository to fetch all NFTs and metadata.
 */
@Singleton
class NFTRepository @Inject constructor(
    private val nftInfraFactory: NftInfraFactory
) {

    suspend fun getAllUserMintyFreshNfts(publicKey: String): List<NFT> {
        val pubKey = PublicKey(publicKey)
        return getAllNfts(pubKey).filter { allUserNFts ->
            allUserNFts.creators.firstOrNull { nft -> nft.address == pubKey } != null &&
                    allUserNFts.creators.firstOrNull { nft -> nft.address.toBase58() == mintyFreshCreatorPubKey } != null
        }
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