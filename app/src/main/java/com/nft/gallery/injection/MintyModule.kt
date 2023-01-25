package com.nft.gallery.injection

import android.content.Context
import android.content.SharedPreferences
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.nft.gallery.BuildConfig
import com.nft.gallery.endpoints.*
import com.nft.gallery.metaplex.MetaplexHttpDriver
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.*
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyModule {

    @Provides
    fun providesNftStorageApi(): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.NFTSTORAGE_API_BASE_URL)
            .addConverterFactory(SerializableResponseConverter(NftStorageResponse.serializer()))
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }

    @Provides
    fun providesShadowDriveApi(): ShadowDriveEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SHADOW_DRIVE_API_BASE_URL)
            .addConverterFactory(SerializableResponseConverter(ShadowDriveResponse.serializer()))
            .build()

        return retrofit.create(ShadowDriveEndpoints::class.java)
    }

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("MainPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    fun providesMobileWalletAdapter(): MobileWalletAdapter {
        return MobileWalletAdapter()
    }

    @Provides
    fun providesMetaplexConnectionDriver(): Connection =
        SolanaConnectionDriver(
            MetaplexHttpDriver(BuildConfig.SOLANA_RPC_URL),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )
}