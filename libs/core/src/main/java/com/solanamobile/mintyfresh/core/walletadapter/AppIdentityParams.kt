package com.solanamobile.mintyfresh.core.walletadapter

import android.net.Uri

data class AppIdentityParams(
    val identityUri: Uri,
    val iconUri: Uri,
    val identityName: String
)