package com.solanamobile.mintyfresh.networkinterface.rpcconfig
import com.solana.mobilewalletadapter.clientlib.RpcCluster

interface IRpcConfig {

    fun getSolanaRpcUrl(): String

    fun getRpcCluster(): RpcCluster
}