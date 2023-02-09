package com.solanamobile.mintyfresh.networkinterface.rpcconfig

import com.solana.mobilewalletadapter.clientlib.RpcCluster

/**
 * RPC config interface
 */
interface IRpcConfig {

    /**
     * A Solana RPC url.
     * @see [https://docs.solana.com/api/http] for API docs
     */
    val solanaRpcUrl: String

    /**
     * cluster where the RPC requests are made.
     */
    val rpcCluster: RpcCluster
}