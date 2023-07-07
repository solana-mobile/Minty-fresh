package com.solanamobile.mintyfresh.mintycore.usecase

import com.solanamobile.mintyfresh.mintycore.ipld.*
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import com.solanamobile.mintyfresh.mintycore.repository.StorageUploadRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class NftCar(val car: CarFile, val metadataCid: CID) {
    val rootCid = car.rootCid
    fun serialize() = car.serialize()
}

class CarFileUseCase @Inject constructor(
    private val cidUseCase: Web3IdUseCase,
    private val storageRepository: StorageUploadRepository
) {

    fun buildNftCar(title: String, description: String, imageFilePath: String): NftCar {

        // first get the media file info (metadata depends on the file cid)
        val mediaFile = File(imageFilePath)
        val mediaFileExtension = mediaFile.extension
        val mediaMimeType = Files.probeContentType(mediaFile.toPath())
        val mediaFileName = "$title.$mediaFileExtension"
        val mediaFileBytes = mediaFile.readBytes()

        lateinit var metadataCid: CID
        return NftCar(CarFileDirectory.Builder(cidUseCase)
            .addFile(mediaFileName, mediaFileBytes) { mediaFileCid ->

                // build the nft metadata (json)
                val imageUrl = storageRepository.getIpfsLinkForCid(mediaFileCid)
                val metadataJson = buildNftMetadata(title, description, imageUrl, mediaMimeType)

                // get the metadata file info
                val metadataBytes = metadataJson.encodeToByteArray()

                addFile("$title.json", metadataBytes) { cid ->
                    metadataCid = cid
                }
            }
            .build(), metadataCid = metadataCid
        )
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

    companion object {
        private const val MAX_BLOCK_SIZE = 1 shl 20 // 1MB
    }
}