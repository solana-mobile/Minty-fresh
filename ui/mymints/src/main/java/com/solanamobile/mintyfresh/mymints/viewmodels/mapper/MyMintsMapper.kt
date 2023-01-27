package com.solanamobile.mintyfresh.mymints.viewmodels.mapper

import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.BuildConfig
import com.nft.gallery.diskcache.MyMint
import com.nft.gallery.viewmodel.viewstate.MyMintsViewState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsMapper @Inject constructor() {

    fun mapLoading() = MyMintsViewState.Loading(
        MutableList(10) { index -> MyMint(index.toString(), "", "", "", "", "") }
    )

    fun map(nfts: List<NFT>) = nfts.map { nft ->
        MyMint(
            id = nft.mint.toString(),
            name = "",
            description = "",
            mediaUrl = "",
            pubKey = nft.updateAuthority.toString(),
            cluster = BuildConfig.RPC_CLUSTER.name
        )
    }

    fun map(nft: NFT, metadata: JsonMetadata) = metadata.image?.let { imageUrl ->
        MyMint(
            id = nft.mint.toString(),
            name = metadata.name,
            description = metadata.description,
            mediaUrl = imageUrl,
            pubKey = nft.updateAuthority.toString(),
            cluster = BuildConfig.RPC_CLUSTER.name
        )
    }
}