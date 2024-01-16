package com.solanamobile.mintyfresh.networkinterface.rpcconfig

import com.solana.mobilewalletadapter.clientlib.Blockchain
import com.solana.mobilewalletadapter.clientlib.Solana
import com.solana.mobilewalletadapter.common.ProtocolContract

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
     * blockchain where requests/transactions are submit.
     */
    val blockchain: Blockchain
}

val IRpcConfig.clusterName: String get() = when (blockchain) {
    Solana.Devnet -> ProtocolContract.CLUSTER_DEVNET
    Solana.Testnet -> ProtocolContract.CLUSTER_TESTNET
    Solana.Mainnet -> ProtocolContract.CLUSTER_MAINNET_BETA
    else -> blockchain.cluster
}