package com.solanamobile.mintyfresh.networkconfigs

import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.networkinterface.usecase.IMyMintsUseCase
import com.solanamobile.mintyfresh.networkinterfaceimpl.rpcconfig.RpcConfig
import com.solanamobile.mintyfresh.networkinterfaceimpl.usecase.MyMintsUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class NetworkModule {

    @Binds
    abstract fun bindsMyMintUseCase(myMintsUseCase: MyMintsUseCase): IMyMintsUseCase

    @Binds
    abstract fun bindsRpcConfig(rpcConfig: RpcConfig): IRpcConfig
}