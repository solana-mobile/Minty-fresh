package com.solanamobile.mintyfresh.mintycore.metaplex

import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.solana.Connection
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import io.ipfs.multibase.binary.Base64
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

private class MessageFeeRequest(message: String) : RpcRequest() {
    override val method = "getFeeForMessage"
    override val params = buildJsonArray {
        add(message)
    }
}

suspend fun Connection.getMessageFee(message: ByteArray) =
    get(
        MessageFeeRequest(Base64.encodeBase64String(message)),
        SolanaResponseSerializer(Long.serializer())
    )