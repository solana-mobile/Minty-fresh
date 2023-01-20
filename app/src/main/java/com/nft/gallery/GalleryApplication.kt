package com.nft.gallery

import android.app.Application
import android.net.Uri
import dagger.hilt.android.HiltAndroidApp

val identityUri: Uri = Uri.parse(BuildConfig.IDENTITY_URI)
val iconUri: Uri = Uri.parse(BuildConfig.IDENTITY_ICO)
const val identityName = BuildConfig.IDENTITY_NAME

@HiltAndroidApp
class GalleryApplication: Application()

