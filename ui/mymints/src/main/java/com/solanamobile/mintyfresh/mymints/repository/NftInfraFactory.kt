package com.solanamobile.mintyfresh.mymints.repository

import com.metaplex.lib.drivers.indenty.ReadOnlyIdentityDriver
import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.drivers.storage.OkHttpSharedStorageDriver
import com.metaplex.lib.modules.nfts.NftClient
import com.solana.core.PublicKey
import okhttp3.OkHttpClient
import java.net.URL
import javax.inject.Inject

class NftInfraFactory @Inject constructor() {

    val storageDriver = OkHttpSharedStorageDriver(OkHttpClient())

    fun createNftClient(pubkey: PublicKey): NftClient {
        val connection = SolanaConnectionDriver(
            JdkRpcDriver(URL("https://api.devnet.solana.com")),  //TODO: This will come from networking layer
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )

        val identityDriver = ReadOnlyIdentityDriver(pubkey, connection)
        val nftClient = NftClient(connection, identityDriver)

        return nftClient
    }

}