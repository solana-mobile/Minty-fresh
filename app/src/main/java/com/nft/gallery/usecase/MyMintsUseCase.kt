package com.nft.gallery.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.metaplex.MintyFreshCreatorPda
import com.nft.gallery.repository.NFTRepository
import com.solana.core.PublicKey

class MyMintsUseCase(publicKey: PublicKey) {

    private val nftRepository = NFTRepository(publicKey)
    private val mintyFreshCreatorPda = MintyFreshCreatorPda(publicKey)

    suspend fun getAllUserMintyFreshNfts(): List<NFT> =
        nftRepository.getAllNfts().filter {
            it.creators.find { it.address == mintyFreshCreatorPda } != null
        }

    suspend fun getNftsMetadata(nft: NFT) = nftRepository.getNftsMetadata(nft)
}