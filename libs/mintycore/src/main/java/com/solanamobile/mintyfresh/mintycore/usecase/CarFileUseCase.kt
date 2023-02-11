package com.solanamobile.mintyfresh.mintycore.usecase

import com.solanamobile.mintyfresh.mintycore.ipld.*
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import com.solanamobile.mintyfresh.mintycore.repository.StorageUploadRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class CarFileUseCase @Inject constructor(
    private val cidUseCase: Web3IdUseCase,
    private val storageRepository: StorageUploadRepository
) {

    val MAX_BLOCK_SIZE = 1 shl 20 // 1MB

    fun buildNftCar(title: String, description: String, imageFilePath: String): CarFile {

        // first get the media file info (metadata depends on the file cid)
        val mediaFile = File(imageFilePath)
        val mediaFileExtension = mediaFile.extension
        val mediaMimeType = Files.probeContentType(mediaFile.toPath())
        val fileBytes = mediaFile.readBytes()

        val mediaFileBlocks = fileBytes.asIterable().chunked(MAX_BLOCK_SIZE).associate {
            val chunkBytes = it.toByteArray()
            cidUseCase.getContentId(chunkBytes) to chunkBytes
        }

        val mediaFileRoot = IpdlFile(mediaFileBlocks.map { (cid, data) ->
            PBLink(null, data.size, cid)
        }).encode()

        val mediaFileCid = cidUseCase.getRootContentId(mediaFileRoot)
        val imageUrl = storageRepository.getNftStorageLinkForCid(mediaFileCid)

        // build the nft metadata (json)
        val metadataJson = buildNftMetadata(title, description, imageUrl, mediaMimeType)

        // get the metadata file info
        val metadataBytes = metadataJson.encodeToByteArray()
        val metadataCid  = cidUseCase.getContentId(metadataBytes)

        // Build the root node, to store both files in an IPLD bucket
        val rootNode = IpdlDirectory(listOf(
            PBLink("$title.json", metadataBytes.size, metadataCid),
            PBLink("$title.$mediaFileExtension", mediaFileRoot.size, mediaFileCid)
        )).encode()

        val rootCid = cidUseCase.getRootContentId(rootNode)

        return CarFile(rootCid)
            .apply {
                mediaFileBlocks.forEach { (cid, data) ->
                    add(cid, data)
                }
            }
            .add(mediaFileCid, mediaFileRoot)
            .add(metadataCid, metadataBytes)
            .add(rootCid, rootNode)
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