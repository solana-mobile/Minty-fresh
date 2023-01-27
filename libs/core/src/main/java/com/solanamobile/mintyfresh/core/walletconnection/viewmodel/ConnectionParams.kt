package com.solanamobile.mintyfresh.core.walletconnection.viewmodel

import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.RpcCluster

data class ConnectionParams(
    val identityUri: Uri,
    val iconUri: Uri,
    val identityName: String,
    val rpcCluster: RpcCluster = RpcCluster.Devnet,
)