package com.solanamobile.mintyfresh.mintycore.usecase

import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import org.intellij.lang.annotations.Language
import java.util.*
import javax.inject.Inject

class XWeb3AuthUseCase @Inject constructor(
    private val didUseCase: Web3IdUseCase,
    private val rpcConfig: IRpcConfig
) {

    fun buildxWeb3AuthMessage(user: PublicKey, rootCid: String): String {

        val did = didUseCase.getDecentralizedIdForUser(user)

        // TODO: these language tags are useful in dev but should probably be removed
        @Language("json")
        val tokenHeader = """
            {
              "alg": "EdDSA",
              "typ": "JWT"
            }
        """.trimIndent()

        @Language("json")
        // TODO: tags used here should be configured based on project settings
        val tokenPayload = """
            {
              "iss": "$did",
              "req": {
                "put": {
                  "rootCID": "$rootCid",
                  "tags": {
                    "mintingAgent": "solana-mobile/Minty-fresh",
                    "chain": "solana",
                    "solanaCluster": "${rpcConfig.rpcCluster.name}"
                  }
                }
              }
            }
        """.trimIndent()

        val headerString = Base64.getUrlEncoder().encodeToString(tokenHeader.toByteArray())
        val payloadString = Base64.getUrlEncoder().encodeToString(tokenPayload.toByteArray())

        return "$headerString.$payloadString"
    }

    fun buildXWeb3AuthToken(message: String, signature: ByteArray) =
        "$message.${Base64.getUrlEncoder().encodeToString(signature)}"
}