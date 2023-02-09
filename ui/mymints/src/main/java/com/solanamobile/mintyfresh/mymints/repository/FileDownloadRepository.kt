package com.solanamobile.mintyfresh.mymints.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

class FileDownloadRepository @Inject constructor(
    private val client: OkHttpClient
) {

    suspend fun downloadFileByUrl(url: String, cacheDir: File) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        val inputStream = response.body?.byteStream()
        val file = File(cacheDir, "share.jpg")

        inputStream?.let {
            file.writeBytes(it.readBytes())
            it.close()
        }

        file
    }
}