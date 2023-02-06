package com.solanamobile.mintyfresh.mintycore.repository

import android.util.Log
import com.metaplex.lib.drivers.network.HttpNetworkDriver
import com.metaplex.lib.drivers.network.HttpRequest
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.*
import com.metaplex.lib.serialization.serializers.solana.AnchorAccountSerializer
import com.solana.core.PublicKey
import com.solana.core.Sysvar
import com.solana.core.Transaction
import com.solana.programs.SystemProgram
import com.solana.programs.TokenProgram
import com.solanamobile.mintyfresh.mintycore.BuildConfig
import com.solanamobile.mintyfresh.mintycore.endpoints.CreateAccountResponse
import com.solanamobile.mintyfresh.mintycore.endpoints.ShadowDriveEndpoints
import com.solanamobile.mintyfresh.mintycore.endpoints.UploadResponse
import com.solanamobile.mintyfresh.mintycore.metaplex.jen.shadowdrive.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import javax.inject.Inject

class ShadowDriveAccountRepository @Inject constructor(
    private val endpoints: ShadowDriveEndpoints
) {

    private val connection = SolanaConnectionDriver(
        JdkRpcDriver(URL("https://api.mainnet-beta.solana.com")),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )

    suspend fun getConfigurationInfo() = withContext(Dispatchers.IO) {
        connection.getAccountInfo(
            AnchorAccountSerializer("storageConfig", storageConfig.serializer()),
            STORAGE_CONFIG_PDA.address
        )
    }

    suspend fun getStorageAccountForOwner(owner: PublicKey, accountNumber: Int) =
        withContext(Dispatchers.IO) {
            val storageAccountAddress = getStorageAccountAddress(owner, accountNumber.toUInt())
            getStorageAccount(storageAccountAddress)
        }

    suspend fun createStorageAccount(serializedTransaction: ByteArray) =
        withContext(Dispatchers.IO) {

            Log.d("SHADOW DRIVE", "making request to create account")

            val body = buildJsonObject {
                put("transaction", Base64.getEncoder().encodeToString(serializedTransaction))
                put("commitment", "processed")
            }

            // temporary, using debug network driver
            MyJdkHttpDriver().makeHttpRequest(object : HttpRequest {
                override val body = body.toString()
                override val method = "POST"
                override val properties: Map<String, String> = mapOf("Content-Type" to "application/json")
                override val url = "${BuildConfig.SHADOW_DRIVE_API_BASE_URL}/storage-account"
            }).run {
                Json.decodeFromString(CreateAccountResponse.serializer(), this)
            }

//            endpoints.createStorageAccount(body.toString().toRequestBody("application/json".toMediaType()))
        }

    suspend fun uploadNftMedia(owner: PublicKey, storageAccount: PublicKey, nameBase: String, imageFile: File, json: String, signedMessage: String) =
        withContext(Dispatchers.IO) {

            Log.d("SHADOW DRIVE", "uploading NFT media and metadata files")

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "$nameBase-media.png", imageFile.asRequestBody("image/png".toMediaType()))
                .addFormDataPart("file", nameBase, json.toRequestBody("application/json".toMediaType()))
                .addFormDataPart("fileNames", "$nameBase-media, $nameBase")
                .addFormDataPart("message", signedMessage)
                .addFormDataPart("signer", owner.toBase58())
                .addFormDataPart("storage_account", storageAccount.toBase58())
                .build()

            // temporary, convert body to string so I can use my debug network driver
            val buffer = Buffer()
            body.writeTo(buffer)
            val stringBody =  buffer.readUtf8()

            MyJdkHttpDriver().makeHttpRequest(object : HttpRequest {
                override val body = stringBody
                override val method = "POST"
                override val properties: Map<String, String> = mapOf("Content-Type" to "multipart/form-data; boundary=${body.boundary}")
                override val url = "${BuildConfig.SHADOW_DRIVE_API_BASE_URL}/upload"
            }).run {
                Json.decodeFromString(UploadResponse.serializer(), this)
            }

//            endpoints.uploadFiles(body)
        }

    suspend fun buildCreateStorageAccountTransaction(accountName: String, requestedStorage: ULong, payer: PublicKey) =
        Transaction().apply {

            val storage = if (requestedStorage < MIN_ACCOUNT_SIZE) MIN_ACCOUNT_SIZE else requestedStorage

            feePayer = payer

            val userInfo = getUserInfoAddress(payer)
            val userInfoAccount = withContext(Dispatchers.IO) {
                getUserInfoAccount(userInfo).getOrNull()
            }
//            println("++++ SHADOW DRIVE user info addy: ${userInfo.toBase58()}")
//            println("++++ SHADOW DRIVE user info: ${userInfoAccount?.data}")
//            println("++++ SHADOW DRIVE storage account 0: ${getStorageAccount(getStorageAccountAddress(payer, 0u)).getOrNull()?.data}")
//            println("++++ SHADOW DRIVE storage account 1: ${getStorageAccount(getStorageAccountAddress(payer, 1u)).getOrNull()?.data}")

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
                    storage = storage
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
                byteArrayOf(
                    ((accountCounter shr 24) and 255u).toByte(),
                    ((accountCounter shr 16) and 255u).toByte(),
                    ((accountCounter shr 8) and 255u).toByte(),
                    (accountCounter and 255u).toByte(),
                ).reversedArray()
            ),
            PROGRAM_ADDRESS
        ).address

    private fun getStakeAccountAddress(storageAccountAddress: PublicKey) =
        PublicKey.findProgramAddress(
            listOf("stake-account".toByteArray(Charsets.UTF_8), storageAccountAddress.toByteArray()),
            PROGRAM_ADDRESS
        ).address

    private suspend fun getUserInfoAccount(accountAddress: PublicKey) =
        connection.getAccountInfo(
            AnchorAccountSerializer("userInfo", userInfo.serializer()),
            accountAddress
        )

    private suspend fun getStorageAccount(accountAddress: PublicKey) =
        connection.getAccountInfo(
            AnchorAccountSerializer("storageAccountV2", storageAccountV2.serializer()),
            accountAddress
        )
}

// TODO: temporary - use retrofit
class MyJdkHttpDriver : HttpNetworkDriver {
    override suspend fun makeHttpRequest(request: HttpRequest): String =
        suspendCancellableCoroutine { continuation ->

            with(URL(request.url).openConnection() as HttpURLConnection) {
                // config
                requestMethod = request.method
                request.properties.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // cancellation
                continuation.invokeOnCancellation { disconnect() }

                // send request body
                request.body?.run {
                    doOutput = true
                    outputStream.write(toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                    outputStream.close()
                }

                // read response
                val responseString = //inputStream.bufferedReader().use { it.readText() }
                    try {
                        inputStream.bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        try {
                            errorStream.bufferedReader().use { it.readText() }
                        } catch (e: Exception) {
                            "No Response"
                        }
                    }

                // TODO: should check response code and/or errorStream for errors
//            println("URL : $url")
//            println("Response Code : $responseCode")
//            println("input stream : $responseString")

                continuation.resumeWith(Result.success(responseString))
            }
        }
}