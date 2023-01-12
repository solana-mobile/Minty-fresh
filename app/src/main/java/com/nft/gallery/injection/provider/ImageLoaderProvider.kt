package com.nft.gallery.injection.provider

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoaderProvider @Inject constructor(
    @ApplicationContext val applicationContext: Context,
) {

    companion object {
        private const val MAX_COIL_MEMORY_CACHE_SIZE_PERCENT = 0.20
        private const val MAX_COIL_DISK_CACHE_SIZE_PERCENT = 0.01
    }

    val imageLoader = ImageLoader.Builder(applicationContext)
        .memoryCache {
            MemoryCache.Builder(applicationContext)
                .maxSizePercent(MAX_COIL_MEMORY_CACHE_SIZE_PERCENT)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(applicationContext.cacheDir.resolve("image_cache"))
                .maxSizePercent(MAX_COIL_DISK_CACHE_SIZE_PERCENT)
                .build()
        }
        .build()
}