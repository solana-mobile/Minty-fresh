package com.nft.gallery.viewmodel.mapper

import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.viewmodel.MyMint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsMapper @Inject constructor() {

    fun map(nft: NFT, metadata: JsonMetadata) = metadata.image?.let { imageUrl ->
        MyMint(nft.mint.toString(), metadata.name, metadata.description, imageUrl)
    }
}