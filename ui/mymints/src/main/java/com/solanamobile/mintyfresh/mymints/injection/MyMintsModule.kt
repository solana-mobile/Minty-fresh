package com.solanamobile.mintyfresh.mymints.injection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient

@InstallIn(
    ViewModelComponent::class
)
@Module
class MyMintsModule {

    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient()
    }

}