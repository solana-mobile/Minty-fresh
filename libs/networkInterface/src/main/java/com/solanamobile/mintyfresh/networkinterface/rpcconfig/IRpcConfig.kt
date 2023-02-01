package com.solanamobile.mintyfresh.networkinterface.rpcconfig
import com.solana.mobilewalletadapter.clientlib.RpcCluster

interface IRpcConfig {

    val solanaRpcUrl: String

    val rpcCluster: RpcCluster
}