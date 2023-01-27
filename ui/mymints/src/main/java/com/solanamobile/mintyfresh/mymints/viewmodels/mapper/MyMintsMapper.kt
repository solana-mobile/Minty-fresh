package com.solanamobile.mintyfresh.mymints.viewmodels.mapper

import com.metaplex.lib.modules.nfts.models.JsonMetadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solanamobile.mintyfresh.mymints.diskcache.MyMint
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MyMintsViewState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyMintsMapper @Inject constructor(
    private val rpcCluster: RpcCluster
) {

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
            cluster = rpcCluster.name
        )
    }

    fun map(nft: NFT, metadata: JsonMetadata) = metadata.image?.let { imageUrl ->
        MyMint(
            id = nft.mint.toString(),
            name = metadata.name,
            description = metadata.description,
            mediaUrl = imageUrl,
            pubKey = nft.updateAuthority.toString(),
            cluster = rpcCluster.name
        )
    }
}