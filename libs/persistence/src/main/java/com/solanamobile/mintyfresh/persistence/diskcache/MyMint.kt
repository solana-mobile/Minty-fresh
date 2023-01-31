package com.solanamobile.mintyfresh.persistence.diskcache

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solana.mobilewalletadapter.common.ProtocolContract.CLUSTER_DEVNET

@Entity(tableName = "MyMint")
data class MyMint(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "nft_name") val name: String?,
    @ColumnInfo(name = "nft_description") val description: String?,
    @ColumnInfo(name = "nft_media_url") val mediaUrl: String,
    @ColumnInfo(
        name = "rpc_cluster",
        defaultValue = CLUSTER_DEVNET
    ) val cluster: String = RpcCluster.Devnet.name,
    @ColumnInfo(name = "pub_key") val pubKey: String
)