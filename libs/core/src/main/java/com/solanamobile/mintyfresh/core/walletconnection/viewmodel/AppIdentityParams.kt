package com.solanamobile.mintyfresh.core.walletconnection.viewmodel

import android.net.Uri

val identityUri: Uri = Uri.parse("https://solanamobile.com")
val iconUri: Uri = Uri.parse("favicon.ico")
const val appName = "Minty Fresh"

val mintyFreshIdentity = AppIdentityParams(identityUri, iconUri, appName)

data class AppIdentityParams(
    val identityUri: Uri,
    val iconUri: Uri,
    val identityName: String
)