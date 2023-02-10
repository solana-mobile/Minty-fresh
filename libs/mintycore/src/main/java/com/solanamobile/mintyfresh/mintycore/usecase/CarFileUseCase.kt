package com.solanamobile.mintyfresh.mintycore.usecase

import com.solanamobile.mintyfresh.mintycore.ipld.*
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class CarFileUseCase @Inject constructor(private val cidUseCase: CidUseCase) {

    val MAX_BLOCK_SIZE = 1 shl 20 // 1MB

    val MAX_FILE_SIZE = MAX_BLOCK_SIZE * 90 // 90 MB

    fun buildNftMetadataCar(title: String, description: String, imageUrl: String): CarWriter {
        val metadataJson = Json.encodeToString(
            JsonMetadata(
                name = title,
                description = description,
                image = imageUrl,
                attributes = listOf(
                    JsonMetadata.Attribute("Minty Fresh", "true")
                ),
                properties = JsonMetadata.Properties(
                    files = listOf(
                        JsonMetadata.Properties.File(imageUrl, "image/png")
                    ),
                    category = "image"
                )
            )
        )

        val metadataBytes = metadataJson.encodeToByteArray()
        val metadataCid  = cidUseCase.getCid(metadataBytes)

        return CarWriter(metadataCid)
            .add(metadataCid, metadataBytes)
    }

    fun buildNftImageCar(filePath: String): CarWriter {
        val uploadFile = File(filePath)
        val fileBytes = uploadFile.readBytes()
        val fileCid  = cidUseCase.getCid(fileBytes)

        return CarWriter(fileCid)
            .add(fileCid, fileBytes)
    }

    fun buildNftCar(title: String, description: String, imageFilePath: String): CarWriter {

        // first get the media file info (metadata depends on the file cid)
        val mediaFile = File(imageFilePath)
        val fileBytes = mediaFile.readBytes()

        val mediaFileBlocks = fileBytes.asIterable().chunked(MAX_BLOCK_SIZE).associate {
            val chunkBytes = it.toByteArray()
            cidUseCase.getCid(chunkBytes) to chunkBytes
        }

        val mediaFileRoot = IpdlFile(mediaFileBlocks.map { (cid, data) ->
            PBLink(null, data.size, cid)
        }).encode()

        val mediaFileCid = cidUseCase.getRootCid(mediaFileRoot)

        // TODO: get this link from repository layer
        val imageUrl = "https://${mediaFileCid.toCanonicalString()}.ipfs.nftstorage.link"
        // "https://ipfs.io/ipfs/${fileCid}"

        // build the nft metadata (json)
        val metadataJson = Json.encodeToString(
            JsonMetadata(
                name = title,
                description = description,
                image = imageUrl,
                attributes = listOf(
                    JsonMetadata.Attribute("Minty Fresh", "true")
                ),
                properties = JsonMetadata.Properties(
                    files = listOf(
                        JsonMetadata.Properties.File(imageUrl, "image/png")
                    ),
                    category = "image"
                )
            )
        )

        // get the metadata file info
        val metadataBytes = metadataJson.encodeToByteArray()
        val metadataCid  = cidUseCase.getCid(metadataBytes)

        // Build the root node, to store both files in an IPLD bucket
        val rootNode = IpdlDirectory(listOf(
            PBLink("$title.json", metadataBytes.size, metadataCid),
            PBLink("$title.png", mediaFileRoot.size, mediaFileCid)
        )).encode()

        val rootCid = cidUseCase.getRootCid(rootNode)

        return CarWriter(rootCid)
            .apply {
                mediaFileBlocks.forEach { (cid, data) ->
                    add(cid, data)
                }
            }
            .add(mediaFileCid, mediaFileRoot)
            .add(metadataCid, metadataBytes)
            .add(rootCid, rootNode)
    }
}