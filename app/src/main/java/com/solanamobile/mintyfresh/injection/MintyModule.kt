package com.solanamobile.mintyfresh.injection

import android.content.Context
import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solanamobile.mintyfresh.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyModule {

    @Provides
    fun providesMobileWalletAdapter(@ApplicationContext context: Context): MobileWalletAdapter {
        return MobileWalletAdapter(ConnectionIdentity(
            identityUri = Uri.parse(context.getString(R.string.id_url)),
            iconUri = Uri.parse(context.getString(R.string.id_favico)),
            identityName = context.getString(R.string.app_name)
        ))
    }
}