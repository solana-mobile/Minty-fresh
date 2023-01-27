package com.solanamobile.mintyfresh.mymints.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.core.pda.mintyFreshCreatorPda
import com.solanamobile.mintyfresh.mymints.repository.NFTRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MyMintsUseCase @AssistedInject constructor(
    @Assisted private val publicKey: PublicKey,
    nftRepositoryFactory: NFTRepository.NFTRepositoryFactory
) {

    @AssistedFactory
    interface MyMintsUseCaseFactory {
        fun create(publicKey: PublicKey): MyMintsUseCase
    }

    private val nftRepository = nftRepositoryFactory.create(publicKey)

    suspend fun getAllUserMintyFreshNfts(): List<NFT> =
        nftRepository.getAllNfts().filter { allUserNFts ->
            allUserNFts.creators.firstOrNull { nft -> nft.address == publicKey } != null &&
                    allUserNFts.creators.firstOrNull { nft -> nft.address == mintyFreshCreatorPda } != null
        }

    suspend fun getNftsMetadata(nft: NFT) = nftRepository.getNftsMetadata(nft)
}