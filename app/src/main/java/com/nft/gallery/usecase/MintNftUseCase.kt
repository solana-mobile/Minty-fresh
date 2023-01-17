/*
 * MintNftUseCase
 * Gallery
 * 
 * Created by Funkatronics on 1/17/2023
 */

package com.nft.gallery.usecase

import com.metaplex.lib.drivers.indenty.IdentityDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.metaplex.lib.experimental.jen.tokenmetadata.Creator
import com.metaplex.lib.modules.nfts.NftClient
import com.metaplex.lib.modules.nfts.builders.CreateNftTransactionBuilder
import com.metaplex.lib.modules.nfts.models.Metadata
import com.metaplex.lib.modules.nfts.models.NFT
import com.nft.gallery.BuildConfig
import com.nft.gallery.metaplex.MetaplexHttpDriver
import com.nft.gallery.metaplex.MintyFreshCreatorPda
import com.solana.core.PublicKey

class MintNftUseCase(private val identityDriver: IdentityDriver) {

    private val rpcUrl = BuildConfig.SOLANA_RPC_URL
    private val connection = SolanaConnectionDriver(
        MetaplexHttpDriver(rpcUrl),
        TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
    )

    val client = NftClient(connection, identityDriver)

    suspend fun mintNft(title: String, metadataUrl: String): Result<NFT> =
        client.create(createNftMetadata(title, metadataUrl))

    // not currently used, but shows how you could build the minting transaction
    suspend fun buildMintTransaction(title: String, metadataUrl: String, mint: PublicKey) =
        CreateNftTransactionBuilder(
            mint,
            createNftMetadata(title, metadataUrl),
            payer = identityDriver.publicKey,
            connection = connection
        ).build().getOrThrow()

    private fun createNftMetadata(title: String, metadataUrl: String) = Metadata(
        name = title,
        uri = metadataUrl,
        sellerFeeBasisPoints = 0,
        creators = listOf(
            Creator(identityDriver.publicKey, true, 100.toUByte()),
            Creator(MintyFreshCreatorPda(identityDriver.publicKey), false, 0.toUByte())
        ),
    )
}