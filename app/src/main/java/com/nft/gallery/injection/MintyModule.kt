package com.nft.gallery.injection

import android.content.Context
import android.content.SharedPreferences
import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.NftStorageEndpoints
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("MainPrefs", Context.MODE_PRIVATE)
    }
}