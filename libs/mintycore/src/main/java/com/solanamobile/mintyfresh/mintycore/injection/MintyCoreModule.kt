package com.solanamobile.mintyfresh.mintycore.injection

import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.solanamobile.mintyfresh.mintycore.BuildConfig
import com.solanamobile.mintyfresh.mintycore.endpoints.*
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyCoreModule {

    @Provides
    fun providesNftStorageApi(okHttpClient: OkHttpClient): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(NftStorageResponseConverter)
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }

    @Provides
    fun providesBundlrApi(okHttpClient: OkHttpClient): BundlrEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BUNDLR_NODE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(BundlrApiConverter)
            .build()

        return retrofit.create(BundlrEndpoints::class.java)
    }

    @Provides
    fun providesMetaplexConnectionDriver(rpcConfig: IRpcConfig): Connection =
        SolanaConnectionDriver(
            JdkRpcDriver(rpcConfig.solanaRpcUrl),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )

    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}