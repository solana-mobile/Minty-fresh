package com.solanamobile.mintyfresh.mintycore.usecase

import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import com.solanamobile.mintyfresh.mintycore.bundlr.Signer
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class DataBundleUseCase @Inject constructor() {

    fun buildMediabundle(imageFilePath: String, owner: Signer): DataItem {
        // first get the media file info (metadata depends on the file id)
        val mediaFile = File(imageFilePath)
        val mediaFileBytes = mediaFile.readBytes()
        val mediaMimeType = Files.probeContentType(mediaFile.toPath())

        return DataItem.Builder()
            .data(mediaFileBytes)
            .tag("Content-Type", mediaMimeType)
            .tag("App-Name", "Minty Fresh")
            .tag("Owner", PublicKey(owner.publicKey).toBase58())
            .owner(owner)
            .build()
    }

    fun buildMetadatabundle(title: String, description: String,
                            mediaDataItem: DataItem, owner: Signer): DataItem {
        val mediaId = mediaDataItem.id
        val mediaUrl = "https://arweave.net/$mediaId"
        val mediaMimeType = mediaDataItem.tags["Content-Type"]
            ?: throw IllegalArgumentException("Provided data item does not have a Content-Type tag")

        val metadata = buildNftMetadata(title, description, mediaUrl, mediaMimeType)
        return DataItem.Builder()
            .data(metadata.encodeToByteArray())
            .tag("Content-Type", "application/json")
            .tag("App-Name", "Minty Fresh")
            .tag("Owner", PublicKey(owner.publicKey).toBase58())
            .owner(owner)
            .build()
    }

    private fun buildNftMetadata(title: String, description: String, imageUrl: String, imageType: String) =
        Json.encodeToString(
            JsonMetadata(
                name = title,
                description = description,
                image = imageUrl,
                attributes = listOf(
                    JsonMetadata.Attribute("Minty Fresh", "true")
                ),
                properties = JsonMetadata.Properties(
                    files = listOf(
                        JsonMetadata.Properties.File(imageUrl, imageType)
                    ),
                    category = "image"
                )
            )
        )
}