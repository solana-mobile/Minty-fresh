package com.solanamobile.mintyfresh.mintycore.metaplex

import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.solana.Connection
import com.solana.core.PublicKey
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

private class AccountBalanceRequest(accountAddress: PublicKey) : RpcRequest() {
    override val method = "getBalance"
    override val params = buildJsonArray {
        add(accountAddress.toBase58())
    }
}

suspend fun Connection.getAccountBalance(accountAddress: PublicKey) =
    get(
        AccountBalanceRequest(accountAddress),
        SolanaResponseSerializer(Long.serializer())
    )