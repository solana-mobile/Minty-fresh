package com.solanamobile.mintyfresh.mintycore.usecase

import com.solanamobile.mintyfresh.mintycore.ipld.CarWriter
import com.solanamobile.mintyfresh.mintycore.metaplex.JsonMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class CarFileUseCase @Inject constructor() {

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
        val metadataCid  = CidUseCase().getCid(metadataBytes)

        return CarWriter(metadataCid)
            .add(metadataCid, metadataBytes)
    }

    fun buildNftImageCar(filePath: String): CarWriter {
        val uploadFile = File(filePath)
        val fileBytes = uploadFile.readBytes()
        val fileCid  = CidUseCase().getCid(fileBytes)

        return CarWriter(fileCid)
            .add(fileCid, fileBytes)
    }

    fun buildNftCar(title: String, description: String, imageFilePath: String): CarWriter {

        val uploadFile = File(imageFilePath)
        val fileBytes = uploadFile.readBytes()
        val fileCid  = CidUseCase().getCid(fileBytes)

        val imageUrl = "https://${fileCid}.ipfs.nftstorage.link"
        // "https://ipfs.io/ipfs/${fileCid}"

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
        val metadataCid  = CidUseCase().getCid(metadataBytes)

        return CarWriter(listOf(metadataCid, fileCid))
            .add(metadataCid, metadataBytes)
            .add(fileCid, fileBytes)
    }
}