package com.nft.gallery.usecase

import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.repository.NFTRepository
import com.solana.core.PublicKey

class MyMintsUseCase(publicKey: PublicKey) {

    private val nftRepository = NFTRepository(publicKey)

    suspend fun getAllNftsForCollectionName(collectionName: String): List<NFT> {
        // Getting all the NFTs that have a Collection
        val nftsWithCollection = nftRepository.getAllNfts()
            .filter { it.collection != null }

        // Finding the collectionName's pubKey (stopping as soon as we find it)
        val iterator = nftsWithCollection.iterator()
        var foundCollection = false
        var collectionPubKey = ""
        while (iterator.hasNext() && !foundCollection) {
            val nft = iterator.next()
            nft.collection?.let { collection ->
                val collectionNft = nftRepository.findByMint(collection.key)
                if (collectionNft.name == collectionName) {
                    collectionPubKey = collection.key.toString()
                    foundCollection = true
                }
            }
        }

        // Filtering the list of NFTs by the collection's pubKey
        return nftsWithCollection
            .filter { it.collection?.key.toString() == collectionPubKey }
    }

    suspend fun getNftsMetadata(nft: NFT) = nftRepository.getNftsMetadata(nft)
}