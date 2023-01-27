package com.nft.gallery.injection

import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.nft.gallery.BuildConfig
import com.nft.gallery.appName
import com.nft.gallery.endpoints.NftStorageEndpoints
import com.nft.gallery.endpoints.NftStorageResponseConverter
import com.nft.gallery.iconUri
import com.nft.gallery.identityUri
import com.nft.gallery.metaplex.MetaplexHttpDriver
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solanamobile.mintyfresh.core.walletconnection.viewmodel.ConnectionParams
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class MintyFreshViewModelModule {

    @Provides
    fun providesNftStorageApi(): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(NftStorageResponseConverter)
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }

    @Provides
    fun providesMobileWalletAdapter(): MobileWalletAdapter {
        return MobileWalletAdapter()
    }

}

@Module
@InstallIn(SingletonComponent::class)
class MintyFreshSingletonModule {
    @Provides
    @Singleton
    fun providesRpcCluster(): RpcCluster = BuildConfig.RPC_CLUSTER

    @Provides
    @Singleton
    fun providesConnectionParams(): ConnectionParams =
        ConnectionParams(
            identityUri = identityUri,
            iconUri = iconUri,
            identityName = appName,
            rpcCluster = BuildConfig.RPC_CLUSTER
        )

    @Provides
    @Singleton
    fun providesMetaplexConnectionDriver(): Connection =
        SolanaConnectionDriver(
            MetaplexHttpDriver(BuildConfig.SOLANA_RPC_URL),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )
}