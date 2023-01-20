package com.nft.gallery

import android.app.Application
import android.net.Uri
import dagger.hilt.android.HiltAndroidApp

val solanaUri: Uri = Uri.parse("https://solanamobile.com")
val iconUri: Uri = Uri.parse("favicon.ico")
const val identityName = "Minty Fresh"

@HiltAndroidApp
class GalleryApplication: Application()

