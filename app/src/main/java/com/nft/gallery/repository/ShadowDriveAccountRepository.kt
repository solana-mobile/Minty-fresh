package com.nft.gallery.repository

import android.util.Log
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.ShadowDriveEndpoints
import com.nft.gallery.endpoints.ShadowRequest
import com.nft.gallery.metaplex.jen.shadowdrive.*
import com.solana.core.PublicKey
import com.solana.core.Sysvar
import com.solana.core.Transaction
import com.solana.programs.SystemProgram
import com.solana.programs.TokenProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.net.URL
import java.util.*
import javax.inject.Inject

class ShadowDriveAccountRepository @Inject constructor(
    private val endpoints: ShadowDriveEndpoints
) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL(BuildConfig.SOLANA_RPC_URL)),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )

    suspend fun getStorageAccountForOwner(owner: PublicKey, accountNumber: Int) =
        withContext(Dispatchers.IO) {
            val storageAccountAddress = getStorageAccountAddress(owner, accountNumber.toUInt())
            getStorageAccount(storageAccountAddress)
        }

    suspend fun createStorageAccount(serializedTransaction: ByteArray) {
        return withContext(Dispatchers.IO) {

            val txString = Base64.getEncoder().encodeToString(serializedTransaction)
            val request = ShadowRequest(txString)

            val result = endpoints.createStorageAccount(request)

            val bucket = result.shadowBucket
            Log.v("Shadow drive", "Your bucket: $bucket")

//            val body = buildJsonObject {
//                put("transaction", Base64.getEncoder().encodeToString(serializedTransaction))
//            }

//            JdkHttpDriver().makeHttpRequest(object : HttpRequest {
//                override val body = body.toString()
//                override val method = "POST"
//                override val properties: Map<String, String> = mapOf("Content-Type" to "application/json")
//                override val url = "${BuildConfig.SHADOW_DRIVE_API_BASE_URL}/storage-account"
//            }).apply {
//                println("++++ SHADOW DIVE RESPONSE: this")
//            }

//            endpoints.createStorageAccount(body.toRequestBody("application/json".toMediaType())).also {
//                println("+++++++ SHADOW DIVE RESPONSE: bucket: ${it.shadowBucket}")
//                println("+++++++ SHADOW DIVE RESPONSE: transactionSignature: ${it.transitionSignature}")
//            }
        }
    }

    suspend fun buildCreateStorageAccountTransaction(accountName: String, requestedStorage: ULong, payer: PublicKey) =
        Transaction().apply {

            val userInfo = getUserInfoAddress(payer)
            val userInfoAccount = withContext(Dispatchers.IO) {
                getUserInfoAccount(userInfo).getOrNull()
            }

            val accountSeed = userInfoAccount?.data?.accountCounter ?: 0u
            val storageAccount = getStorageAccountAddress(payer, accountSeed)
            val stakeAccount = getStakeAccountAddress(storageAccount)
            val ownerAta = PublicKey.associatedTokenAddress(payer, TOKEN_MINT)

            addInstruction(
                ShadowDriveInstructions.initializeAccount2(
                    storageConfig = STORAGE_CONFIG_PDA.address,
                    userInfo = userInfo,
                    storageAccount = storageAccount,
                    stakeAccount = stakeAccount,
                    tokenMint = TOKEN_MINT,
                    owner1 = payer,
                    uploader = UPLOADER,
                    owner1TokenAccount = ownerAta.address,
                    systemProgram = SystemProgram.PROGRAM_ID,
                    tokenProgram = TokenProgram.PROGRAM_ID,
                    rent = Sysvar.SYSVAR_RENT_PUBKEY,
                    identifier = accountName,
                    storage = requestedStorage
                )
            )
        }

    private fun getUserInfoAddress(owner: PublicKey) =
        PublicKey.findProgramAddress(
            listOf("user-info".toByteArray(Charsets.UTF_8), owner.toByteArray()),
            PROGRAM_ADDRESS
        ).address

    private fun getStorageAccountAddress(owner: PublicKey, accountCounter: UInt) =
        PublicKey.findProgramAddress(
            listOf(
                "storage-account".toByteArray(Charsets.UTF_8),
                owner.toByteArray(),
                BigInteger.valueOf(accountCounter.toLong()).toByteArray()
            ),
            PROGRAM_ADDRESS
        ).address

    private fun getStakeAccountAddress(storageAccountAddress: PublicKey) =
        PublicKey.findProgramAddress(
            listOf("stake-account".toByteArray(Charsets.UTF_8), storageAccountAddress.toByteArray()),
            PROGRAM_ADDRESS
        ).address

    private suspend fun getUserInfoAccount(accountAddress: PublicKey) =
        connection.getAccountInfo(userInfo.serializer(), accountAddress)

    private suspend fun getStorageAccount(accountAddress: PublicKey) =
        connection.getAccountInfo(storageAccount.serializer(), accountAddress)
}