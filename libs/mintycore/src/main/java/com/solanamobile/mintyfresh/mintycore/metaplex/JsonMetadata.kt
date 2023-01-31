package com.solanamobile.mintyfresh.mintycore.metaplex

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonMetadata(
    val name: String,
    val description: String,
    val image: String,
    val attributes: List<Attribute>? = null,
    val properties: Properties? = null
) {
    @Serializable
    data class Attribute(
        @SerialName("trait_type") val traitType: String,
        val value: String
    )

    @Serializable
    data class Properties(
        val files: List<File>?,
        val category: String?
    ) {
        @Serializable
        data class File(
            val uri: String,
            val type: String,
            val cdn: Boolean? = null
        )
    }
}



