package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.getRecentBlockhash
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.metaplex.lib.modules.token.MIN_RENT_FOR_MINT
import com.solana.core.HotAccount
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.mintycore.metaplex.simulateTransaction
import com.solanamobile.mintyfresh.networkinterface.pda.mintyFreshCreatorPubKey
import javax.inject.Inject

class MintTransactionRepository @Inject constructor(private val connectionDriver: Connection)  {

    suspend fun estimateFeeForMintTransaction(title: String, payer: PublicKey): Result<Long> {

        // This is the minimum rent, so an under estimation of what we are using =/
        val rent = MIN_RENT_FOR_MINT
        val transactionFee = 10000

        val simulatedUrl = "https://ipfs.io/ipfs/bafyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/$title.png/"

        val tempMintAccount = HotAccount().publicKey
        val transaction = buildMintTransaction(title, simulatedUrl, tempMintAccount, payer)

        val blockHash = connectionDriver.getRecentBlockhash().getOrThrow()
        transaction.setRecentBlockHash(blockHash)

        return connectionDriver.simulateTransaction(transaction).map {
            it?.unitsConsumed?.plus(transactionFee + rent) ?: -1
        }
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