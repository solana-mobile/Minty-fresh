package com.solanamobile.mintyfresh.injection

import android.content.Context
import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solanamobile.mintyfresh.R
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileWalletAdapterProviderImpl@Inject constructor(@ApplicationContext context: Context, rpcConfig: IRpcConfig) :
    MobileWalletAdapterProvider {
    override val mobileWalletAdapter: MobileWalletAdapter = MobileWalletAdapter(ConnectionIdentity(
        identityUri = Uri.parse(context.getString(R.string.id_url)),
        iconUri = Uri.parse(context.getString(R.string.id_favico)),
        identityName = context.getString(R.string.app_name)
    )).also {
        it.blockchain = rpcConfig.blockchain
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MobileWalletAdapterProviderModule {
    @Singleton
    @Binds
    internal abstract fun bindMobileWalletAdapterProvider(
        mobileWalletAdapterProviderImpl: MobileWalletAdapterProviderImpl
    ): MobileWalletAdapterProvider
}