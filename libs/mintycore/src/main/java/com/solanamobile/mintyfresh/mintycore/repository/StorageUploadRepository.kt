package com.solanamobile.mintyfresh.mintycore.repository

import android.content.Context
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.R
import com.solanamobile.mintyfresh.mintycore.bundlr.DataItem
import com.solanamobile.mintyfresh.mintycore.endpoints.BundlrEndpoints
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
    private val bundlrEndpoints: BundlrEndpoints,
    private val nftStorageEndpoints: NftStorageEndpoints
) {

    suspend fun getBundlrNodeInfo(): NodeInfoResponse = bundlrEndpoints.info()
    suspend fun getBundlrPrice(numBytes: Int): Long = bundlrEndpoints.price(numBytes)
    suspend fun getBundlrBalance(address: PublicKey): Long = bundlrEndpoints.balance(address.toBase58())
    suspend fun fundBundlrAccount(txId: String): Boolean = bundlrEndpoints.fund(txId)
    suspend fun upload(dataItem: DataItem): UploadResponse = bundlrEndpoints.uploadDataItem(dataItem)

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