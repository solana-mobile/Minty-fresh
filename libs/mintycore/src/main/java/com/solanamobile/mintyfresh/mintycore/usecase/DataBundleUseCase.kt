package com.solanamobile.mintyfresh.mintycore.usecase

import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import com.solanamobile.mintyfresh.mintycore.bundlr.Signer
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class NftBundle(val metadata: DataItem, val media: DataItem) {
    val metadataId get() = metadata.id
    val mediaId get() = media.id
    suspend fun sign(signer: Signer): String {
        metadata.sign(signer)
        media.sign(signer)
        return metadataId
    }
}

class DataBundleUseCase @Inject constructor() {

    suspend fun buildAndSignNftBundle(title: String, description: String,
                               imageFilePath: String, owner: Signer): NftBundle {

        // first get the media file info (metadata depends on the file id)
        val mediaFile = File(imageFilePath)
        val mediaFileBytes = mediaFile.readBytes()
        val mediaMimeType = withContext(Dispatchers.IO) {
            Files.probeContentType(mediaFile.toPath())
        }

        val mediaDataItem = DataItem.Builder()
            .data(mediaFileBytes)
            .tag("Content-Type", mediaMimeType)
            .tag("App-Name", "Minty Fresh")
            .tag("Owner", PublicKey(owner.publicKey).toBase58())
            .owner(owner)
            .build()

        // media item MUST first be signed in order to obtain valid id
        mediaDataItem.sign(owner)

        val mediaId = mediaDataItem.id
        val mediaUrl = "https://arweave.net/$mediaId"
        val metadata = buildNftMetadata(title, description, mediaUrl, mediaMimeType)
        val metadataItem = DataItem.Builder()
            .data(metadata.encodeToByteArray())
            .tag("Content-Type", "application/json")
            .tag("App-Name", "Minty Fresh")
            .tag("Owner", PublicKey(owner.publicKey).toBase58())
            .owner(owner)
            .build()

        metadataItem.sign(owner)

        return NftBundle(metadataItem, mediaDataItem)
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