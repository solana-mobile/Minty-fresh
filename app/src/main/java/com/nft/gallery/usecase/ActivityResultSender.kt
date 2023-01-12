package com.nft.gallery.usecase

import android.content.Intent

interface ActivityResultSender {
    fun launch(intent: Intent)
}