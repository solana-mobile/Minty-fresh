package com.solanamobile.mintyfresh.networkinterfaceimpl.repository

import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NftInfraFactory @Inject constructor(
    private val rpcConfig: IRpcConfig
) {

    val storageDriver = OkHttpSharedStorageDriver(OkHttpClient())

    fun createNftClient(publicKey: PublicKey): NftClient {
        val connection = SolanaConnectionDriver(
            JdkRpcDriver(rpcConfig.solanaRpcUrl),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )

        val identityDriver = ReadOnlyIdentityDriver(publicKey, connection)

        return NftClient(connection, identityDriver)
    }

}