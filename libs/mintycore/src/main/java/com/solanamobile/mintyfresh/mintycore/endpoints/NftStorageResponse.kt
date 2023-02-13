package com.solanamobile.mintyfresh.mintycore.endpoints

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

@Serializable
data class NftStorageResponse(
    val ok: Boolean,
    val value: FileData?,
    val error: Error? = null
)

@Serializable
data class FileData(
    val cid: String,
    val size: Int
)

@Serializable
data class Error(
    val name: String? = null,
    val message: String
)

object NftStorageResponseConverter : Converter.Factory() {

    private val json = Json { ignoreUnknownKeys = true }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, NftStorageResponse> {
        return Converter { response ->
            json.decodeFromString(NftStorageResponse.serializer(), response.string())
        }
    }

    // not used
    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        return super.requestBodyConverter(
            type,
            parameterAnnotations,
            methodAnnotations,
            retrofit
        )
    }

    // not used
    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        return super.stringConverter(type, annotations, retrofit)
    }
}