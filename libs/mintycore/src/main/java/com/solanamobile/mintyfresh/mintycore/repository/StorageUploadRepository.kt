package com.solanamobile.mintyfresh.mintycore.repository

import android.content.Context
import com.solanamobile.mintyfresh.mintycore.R
import com.solanamobile.mintyfresh.mintycore.endpoints.NftStorageEndpoints
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
    private val endpoints: NftStorageEndpoints
) {

    fun getIpfsLinkForCid(cid: CID) = "$ipfsUrlPrefix${cid.toCanonicalString()}"
    fun getNftStorageLinkForCid(cid: CID) = "https://${cid.toCanonicalString()}.ipfs.nftstorage.link"

    suspend fun uploadCar(car: ByteArray, authToken: String) =
        withContext(Dispatchers.IO) {

            val result = endpoints.uploadCar(
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