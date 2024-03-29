package com.solanamobile.mintyfresh.networkinterfaceimpl.rpcconfig

import com.solana.mobilewalletadapter.clientlib.Blockchain
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.networkinterfaceimpl.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RpcConfig @Inject constructor() : IRpcConfig {
    override val solanaRpcUrl: String = BuildConfig.SOLANA_RPC_URL

    override val blockchain: Blockchain = BuildConfig.BLOCKCHAIN
}