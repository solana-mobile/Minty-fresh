package com.nft.gallery.endpoints

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShadowDriveResponse(
    @SerialName("shdw_bucket") val shadowBucket: String,
    @SerialName("transaction_signature") val transitionSignature: String
)