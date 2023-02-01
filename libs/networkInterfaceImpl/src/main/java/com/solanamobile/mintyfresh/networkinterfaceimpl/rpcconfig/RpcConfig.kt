package com.solanamobile.mintyfresh.networkinterfaceimpl.rpcconfig

import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import com.solanamobile.mintyfresh.networkinterfaceimpl.BuildConfig
import javax.inject.Inject

class RpcConfig @Inject constructor() : IRpcConfig {
    override fun getSolanaRpcUrl(): String = BuildConfig.SOLANA_RPC_URL

    override fun getRpcCluster(): RpcCluster = BuildConfig.RPC_CLUSTER
}