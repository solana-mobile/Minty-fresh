package com.nft.gallery.metaplex

import com.metaplex.lib.drivers.network.HttpPostRequest
import com.metaplex.lib.drivers.network.JdkHttpDriver
import com.metaplex.lib.drivers.rpc.JsonRpcDriver
import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.rpc.RpcResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class MetaplexHttpDriver(private val url: String) : JsonRpcDriver {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override suspend fun <R> makeRequest(request: RpcRequest, resultSerializer: KSerializer<R>): RpcResponse<R> =
        JdkHttpDriver().makeHttpRequest(
            HttpPostRequest(
                url = url,
                properties = mapOf("Content-Type" to "application/json; charset=utf-8"),
                // android.util.Base64 is adding newlines to the encoded payload so fixing that here for now
                body = json.encodeToString(RpcRequest.serializer(), request)
                    .replace("\\n", "")
            )
        ).run {
            json.decodeFromString(
                RpcResponse.serializer(resultSerializer),
                this
            )
        }
}