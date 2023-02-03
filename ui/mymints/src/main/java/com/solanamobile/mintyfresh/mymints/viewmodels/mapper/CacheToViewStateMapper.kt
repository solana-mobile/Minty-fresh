package com.solanamobile.mintyfresh.mymints.viewmodels.mapper

import com.solanamobile.mintyfresh.networkinterface.data.MintedMedia
import com.solanamobile.mintyfresh.persistence.diskcache.MyMint
import javax.inject.Inject

class CacheToViewStateMapper @Inject constructor() {

    fun mapMintToViewState(nft: MyMint): MintedMedia {
        return MintedMedia(
            id = nft.id,
            mediaUrl = nft.mediaUrl,
            name = nft.name ?: "",
            description = nft.description ?: ""
        )
    }

}