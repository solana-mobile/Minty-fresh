package com.solanamobile.mintyfresh.mintycore.injection

import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.solanamobile.mintyfresh.mintycore.BuildConfig
import com.solanamobile.mintyfresh.mintycore.endpoints.NftStorageEndpoints
import com.solanamobile.mintyfresh.mintycore.endpoints.NftStorageResponseConverter
import com.solanamobile.mintyfresh.mintycore.metaplex.MetaplexHttpDriver
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyCoreModule {

    @Provides
    fun providesNftStorageApi(): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(NftStorageResponseConverter)
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }

    @Provides
    fun providesMetaplexConnectionDriver(rpcConfig: IRpcConfig): Connection =
        SolanaConnectionDriver(
            MetaplexHttpDriver(rpcConfig.solanaRpcUrl),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )
}