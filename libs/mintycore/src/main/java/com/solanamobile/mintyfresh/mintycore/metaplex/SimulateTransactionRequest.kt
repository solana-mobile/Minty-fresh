package com.solanamobile.mintyfresh.mintycore.metaplex

import com.metaplex.lib.drivers.rpc.RpcRequest
import com.metaplex.lib.drivers.solana.Connection
import com.solana.core.PublicKey
import com.solana.core.SerializeConfig
import com.solana.core.Transaction
import com.solana.networking.serialization.serializers.solana.SolanaResponseSerializer
import io.ipfs.multibase.binary.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class TransactionSimulation(
    @SerialName("err") val error: String?,
    val logs: List<String>,
    val accounts: List<JsonObject?>?,
    val unitsConsumed: Long
)

private class SimulateTransactionRequest(transaction: String,
                                         accounts: List<PublicKey>? = null
) : RpcRequest() {
    override val method = "simulateTransaction"
    override val params = buildJsonArray {
        add(transaction)
        addJsonObject {
            put("encoding", "base64")
            put("replaceRecentBlockhash", true)
            accounts?.map { JsonPrimitive(it.toBase58()) }?.let { accountsJson ->
                put("accounts", buildJsonObject {
                    put("addresses", JsonArray(accountsJson))
                })
            }
        }
    }
}

suspend fun Connection.simulateTransaction(transaction: Transaction) =
    get(
        SimulateTransactionRequest(
            Base64.encodeBase64String(transaction.serialize(SerializeConfig(verifySignatures = false))),
            transaction.signatures.map { it.publicKey }
        ),
        SolanaResponseSerializer(TransactionSimulation.serializer())
    )