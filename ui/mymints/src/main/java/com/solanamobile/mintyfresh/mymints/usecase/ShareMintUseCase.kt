package com.solanamobile.mintyfresh.mymints.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.solanamobile.mintyfresh.mymints.R
import com.solanamobile.mintyfresh.mymints.repository.FileDownloadRepository
import javax.inject.Inject

class ShareMintUseCase @Inject constructor(
    private val fileDownloadRepository: FileDownloadRepository
) {

    suspend fun createMintShareIntent(ctx: Context, imgUrl: String, mintAddr: String): Intent {
        val outFile = fileDownloadRepository.downloadFileByUrl(imgUrl, ctx.cacheDir)

        val providerName = ctx.packageName + ".provider"
        val uri = FileProvider.getUriForFile(ctx, providerName, outFile)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, ctx.resources.getString(R.string.share_mint_text, mintAddr))
            putExtra(Intent.EXTRA_STREAM, uri)
            setDataAndType(uri, "image/jpeg")

            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        return Intent.createChooser(sendIntent, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}