package com.solanamobile.mintyfresh.mintycore.repository

import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.solana.core.PublicKey
import com.solanamobile.mintyfresh.networkinterface.pda.mintyFreshCreatorPubKey
import javax.inject.Inject

class MintTransactionRepository @Inject constructor(private val connectionDriver: Connection)  {

    // not currently used, but shows how you could build the minting transaction
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