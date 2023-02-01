package com.solanamobile.mintyfresh.networkconfigs

import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.networkinterface.usecase.IMyMintsUseCase
import com.solanamobile.mintyfresh.networkinterfaceimpl.rpcconfig.RpcConfig
import com.solanamobile.mintyfresh.networkinterfaceimpl.usecase.MyMintsUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindsMyMintUseCase(myMintsUseCase: MyMintsUseCase): IMyMintsUseCase

    @Binds
    @Singleton
    abstract fun bindsRpcConfig(rpcConfig: RpcConfig): IRpcConfig
}