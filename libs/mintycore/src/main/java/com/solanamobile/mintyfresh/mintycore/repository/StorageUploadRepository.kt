package com.solanamobile.mintyfresh.mintycore.repository

import android.content.Context
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.R
import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import com.solanamobile.mintyfresh.mintycore.endpoints.ArweaveEndpoints
import com.solanamobile.mintyfresh.mintycore.endpoints.BundlerEndpoints
import com.solanamobile.mintyfresh.mintycore.endpoints.NftStorageEndpoints
import com.solanamobile.mintyfresh.mintycore.endpoints.NodeInfoResponse
import com.solanamobile.mintyfresh.mintycore.endpoints.UploadResponse
import com.solanamobile.mintyfresh.mintycore.ipld.CID
import com.solanamobile.mintyfresh.mintycore.ipld.toCanonicalString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class StorageUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bundlerEndpoints: BundlerEndpoints,
    private val arweaveEndpoints: ArweaveEndpoints,
    private val nftStorageEndpoints: NftStorageEndpoints
) {

    suspend fun getBundlerNodeInfo(): NodeInfoResponse = bundlerEndpoints.info()
    suspend fun getBundlerPrice(numBytes: Int): Long = bundlerEndpoints.price(numBytes)
    suspend fun getAccountBalance(address: PublicKey): Long = bundlerEndpoints.balance(address.toBase58())
    suspend fun fundAccount(txId: String): Boolean = bundlerEndpoints.fund(txId)
    suspend fun upload(dataItem: DataItem): UploadResponse {
        return if (exists(dataItem)) {
            UploadResponse.FileAlreadyUploaded(dataItem.id)
        } else bundlerEndpoints.uploadDataItem(dataItem)
    }
    suspend fun exists(dataItem: DataItem): Boolean {
        val response = arweaveEndpoints.exists(dataItem.id)
        return response.isSuccessful && response.code() == 200
    }

    fun getIpfsLinkForCid(cid: CID) = "$ipfsUrlPrefix${cid.toCanonicalString()}"
    fun getNftStorageLinkForCid(cid: CID) = "https://${cid.toCanonicalString()}.ipfs.nftstorage.link"

    suspend fun uploadCar(car: ByteArray, authToken: String) =
        withContext(Dispatchers.IO) {

            val result = nftStorageEndpoints.uploadCar(
                car.toRequestBody("application/car; charset=utf-8".toMediaType()),
                "Metaplex $authToken"
            )

            result.error?.let { err ->
                throw Error("${context.getString(R.string.upload_file_error_message)}\n${err.name}\n${err.message}")
            }

            "$ipfsUrlPrefix${result.value?.cid}"
        }

    companion object {
        const val ipfsUrlPrefix = "https://ipfs.io/ipfs/"
    }
}