package com.solanamobile.mintyfresh.mymints.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.solanamobile.mintyfresh.mymints.R
import com.solanamobile.mintyfresh.mymints.repository.FileDownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ShareMintUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDownloadRepository: FileDownloadRepository
) {

    suspend fun createMintShareIntent(imgUrl: String, mintAddr: String): Intent {
        val outFile = fileDownloadRepository.downloadFileByUrl(imgUrl, context.cacheDir)

        val providerName = context.packageName + ".provider"
        val uri = FileProvider.getUriForFile(context, providerName, outFile)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${ context.resources.getString(R.string.share_mint_text) }https://solscan.io/token/$mintAddr")
            putExtra(Intent.EXTRA_STREAM, uri)
            setDataAndType(uri, "image/jpeg")

            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        return Intent.createChooser(sendIntent, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}