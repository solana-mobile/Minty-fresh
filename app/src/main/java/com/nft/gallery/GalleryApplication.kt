package com.nft.gallery

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.nft.gallery.injection.provider.ImageLoaderProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GalleryApplication: Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoaderProvider: ImageLoaderProvider

    /**
     * Coil [ImageLoader] configuration (it's a Singleton).
     */
    override fun newImageLoader() = imageLoaderProvider.imageLoader
}

