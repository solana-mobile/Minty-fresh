package com.solanamobile.mintyfresh.mymints.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.core.pda.mintyFreshCreatorPda
import com.solanamobile.mintyfresh.mymints.repository.NFTRepository

class MyMintsUseCase(private val publicKey: PublicKey) {

    private val nftRepository = NFTRepository(publicKey)

    suspend fun getAllUserMintyFreshNfts(): List<NFT> =
        nftRepository.getAllNfts().filter { allUserNFts ->
            allUserNFts.creators.firstOrNull { nft -> nft.address == publicKey } != null &&
            allUserNFts.creators.firstOrNull { nft -> nft.address == mintyFreshCreatorPda } != null
        }

    suspend fun getNftsMetadata(nft: NFT) = nftRepository.getNftsMetadata(nft)
}