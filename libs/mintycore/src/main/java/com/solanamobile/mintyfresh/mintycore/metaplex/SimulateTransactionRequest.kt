package com.solanamobile.mintyfresh.mintycore.metaplex

import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.solana.Connection
import com.solana.core.SerializeConfig
import com.solana.core.Transaction
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import io.ipfs.multibase.binary.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

private class SimulateTransactionRequest(transaction: String) : RpcRequest() {
    override val method = "simulateTransaction"
    override val params = buildJsonArray {
        add(transaction)
        addJsonObject {
            put("encoding", "base64")
        }
    }
}

@Serializable
data class TransactionSimulation(
    val logs: List<String>,
    val unitsConsumed: Long
)

suspend fun Connection.simulateTransaction(transaction: Transaction) =
    get(
        SimulateTransactionRequest(
            Base64.encodeBase64String(transaction.serialize(SerializeConfig(verifySignatures = false)))
        ),
        SolanaResponseSerializer(TransactionSimulation.serializer())
    )