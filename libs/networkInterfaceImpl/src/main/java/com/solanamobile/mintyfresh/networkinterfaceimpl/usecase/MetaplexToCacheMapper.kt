package com.solanamobile.mintyfresh.networkinterfaceimpl.usecase

import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetaplexToCacheMapper @Inject constructor() {

    fun map(nfts: List<NFT>, rpcClusterName: String) = nfts.map { nft ->
        MyMint(
            id = nft.mint.toString(),
            name = "",
            description = "",
            mediaUrl = "",
            pubKey = nft.updateAuthority.toString(),
            cluster = rpcClusterName
        )
    }

    fun map(nft: NFT, metadata: JsonMetadata, rpcClusterName: String) = metadata.image?.let { imageUrl ->
        MyMint(
            id = nft.mint.toString(),
            name = metadata.name,
            description = metadata.description,
            mediaUrl = imageUrl,
            pubKey = nft.updateAuthority.toString(),
            cluster = rpcClusterName
        )
    }
}