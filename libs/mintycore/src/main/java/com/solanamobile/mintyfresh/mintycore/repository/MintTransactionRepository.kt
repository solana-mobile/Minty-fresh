package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.solana.core.HotAccount
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.metaplex.TransactionSimulation
import com.solanamobile.mintyfresh.mintycore.metaplex.simulateTransaction
import com.solanamobile.mintyfresh.networkinterface.pda.mintyFreshCreatorPubKey
import javax.inject.Inject

class MintTransactionRepository @Inject constructor(private val connectionDriver: Connection)  {

    suspend fun simulateMintTransaction(title: String, payer: PublicKey): Result<TransactionSimulation> {
        val simulatedUrl = "https://ipfs.io/ipfs/bafyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/$title.png/"

        val tempMintAccount = HotAccount().publicKey
        val transaction = buildMintTransaction(title, simulatedUrl, tempMintAccount, payer)

        // we can stick a fake block hash in for the simulation
        transaction.setRecentBlockHash("1".repeat(PublicKey.PUBLIC_KEY_LENGTH))

        return connectionDriver.simulateTransaction(transaction).fold(
            { sim ->
                if(sim != null && sim.error == null) return Result.success(sim)
                else return Result.failure(Throwable(sim?.error))
            },
            {
                Result.failure(it)
            }
        )
    }

    suspend fun buildMintTransaction(title: String, metadataUrl: String, mint: PublicKey, payer: PublicKey) =
        CreateNftTransactionBuilder(
            newMint = mint,
            metadata = createNftMetadata(title, metadataUrl, payer),
            payer = payer,
            connection = connectionDriver
        ).build().getOrThrow().apply {
            feePayer = payer
        }

    private fun createNftMetadata(title: String, metadataUrl: String, creator: PublicKey) = Metadata(
        name = title,
        uri = metadataUrl,
        sellerFeeBasisPoints = 0,
        creators = listOf(
            Creator(creator, true, 100.toUByte()),
            Creator(PublicKey(mintyFreshCreatorPubKey), false, 0.toUByte())
        ),
    )
}