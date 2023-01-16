package com.nft.gallery.viewmodel.mapper

import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.diskcache.MyMint
import com.nft.gallery.viewmodel.viewstate.MyMintsViewState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsMapper @Inject constructor() {

    fun mapLoading() = MyMintsViewState.Loading(
        MutableList(28) { index -> MyMint(index.toString(), "", "", "") }
    )

    fun map(nfts: List<NFT>) = nfts.map { nft ->
        MyMint(nft.mint.toString(), "", "", "")
    }

    fun map(nft: NFT, metadata: JsonMetadata) = metadata.image?.let { imageUrl ->
        MyMint(nft.mint.toString(), metadata.name, metadata.description, imageUrl)
    }
}