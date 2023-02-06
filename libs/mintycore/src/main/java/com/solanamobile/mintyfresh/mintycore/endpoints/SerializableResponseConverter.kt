package com.solanamobile.mintyfresh.mintycore.endpoints

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class SerializableResponseConverter<R>(private val responseSerializer: KSerializer<R>) : Converter.Factory() {

    private val json = Json { ignoreUnknownKeys = true }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, R> {
        return Converter { response ->
            json.decodeFromString(responseSerializer, response.string())
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