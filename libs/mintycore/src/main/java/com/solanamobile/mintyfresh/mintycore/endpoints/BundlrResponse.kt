package com.solanamobile.mintyfresh.mintycore.endpoints

import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

@Serializable
data class NodeInfoResponse(
    val version: String,
    val gateway: String,
    val addresses: NodeAddresses
)

@Serializable
data class NodeAddresses(
    // NOTE: there are other chains returned, but we only care about sol
    val solana: String
)

@Serializable
data class BalanceResponse(val balance: Long)

@Serializable
data class FundAccountResponse(val confirmed: Boolean)

sealed interface UploadResponse {
    val id: String

    @Serializable
    data class UploadComplete(override val id: String, val timestamp: Long) : UploadResponse
    data class FileAlreadyUploaded(override val id: String) : UploadResponse
}

object BundlrApiConverter : Converter.Factory() {

    private val json = Json { ignoreUnknownKeys = true }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return when(type) {
            NodeInfoResponse::class.java -> Converter { response ->
                json.decodeFromString(NodeInfoResponse.serializer(), response.string())
            }
            java.lang.Long::class.java -> Converter {
                val response = it.string()
                response.toLongOrNull()
                    ?: json.decodeFromString(BalanceResponse.serializer(), response).balance
            }
            java.lang.Boolean::class.java -> Converter { response ->
                json.decodeFromString(FundAccountResponse.serializer(), response.string()).confirmed
            }
            UploadResponse::class.java -> Converter { response ->
                val responseString = response.string()
                try {
                    json.decodeFromString(UploadResponse.UploadComplete.serializer(), responseString)
                } catch (error: SerializationException) {
                    "^Transaction [a-zA-Z0-9-_=]+ already received\$".toRegex()
                        .matchEntire(responseString)?.value?.split(' ')?.get(1)?.let { id ->
                            UploadResponse.FileAlreadyUploaded(id)
                        }
                }
            }
            else -> super.responseBodyConverter(type, annotations, retrofit)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return when (type) {
            DataItem::class.java -> Converter<DataItem, RequestBody> { it.byteArray.toRequestBody() }
            ByteArray::class.java -> Converter<ByteArray, RequestBody> { it.toRequestBody() }
            java.lang.String::class.java -> Converter<String, RequestBody> { "{\"tx_id\":\"$it\"}".toRequestBody() }
            else -> super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }
    }
}