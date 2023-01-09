package com.nft.gallery.injection

import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyModule {

    @Provides
    fun providesNftStorageApi(): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }
}