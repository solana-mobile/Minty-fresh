package com.solanamobile.mintyfresh.mintycore.endpoints

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

sealed interface ShadowDriveResponse

@Serializable
data class CreateAccountResponse(
    @SerialName("shdw_bucket") val shadowBucket: String,
    @SerialName("transaction_signature") val transitionSignature: String
    // TODO: need to handle error here, it's undocumented in shadow drive
) : ShadowDriveResponse

@Serializable
data class UploadResponse(
    @SerialName("finalized_locations")val finalizedLocations: List<String>,
    @SerialName("message") val message : String,
    @SerialName("upload_errors") val errors: List<UploadError> = listOf()
) : ShadowDriveResponse

@Serializable
data class UploadError(
    val file: String,
    @SerialName("storage_account") val storageAccount: String,
    val error: String
)

object ShadowDriveResponseSerializer
    : JsonContentPolymorphicSerializer<ShadowDriveResponse>(ShadowDriveResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ShadowDriveResponse> = when {
        "shdw_bucket" in element.jsonObject -> CreateAccountResponse.serializer()
        else -> UploadResponse.serializer()
    }
}