/*
 * MintNftUseCase
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.usecase

import com.metaplex.lib.drivers.solana.*
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.nft.gallery.metaplex.MintyFreshCreatorPda
import com.solana.core.PublicKey
import javax.inject.Inject

class BuildMintTransactionUseCase @Inject constructor(private val connectionDriver: Connection)  {

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
            Creator(MintyFreshCreatorPda(creator), false, 0.toUByte())
        ),
    )
}