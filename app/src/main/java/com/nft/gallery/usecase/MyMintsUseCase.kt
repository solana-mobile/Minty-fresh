package com.nft.gallery.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.metaplex.mintyFreshCreatorPda
import com.nft.gallery.repository.NFTRepository
import com.solana.core.PublicKey

class MyMintsUseCase(publicKey: PublicKey) {

    private val nftRepository = NFTRepository(publicKey)

    suspend fun getAllUserMintyFreshNfts(): List<NFT> =
        nftRepository.getAllNfts().filter { allUserNFts ->
            allUserNFts.creators.firstOrNull { nft -> nft.address == mintyFreshCreatorPda } != null
        }

    suspend fun getNftsMetadata(nft: NFT) = nftRepository.getNftsMetadata(nft)
}