/*
 * MintyFreshCreatorPda
 * Gallery
 * 
 * Created by Funkatronics on 1/13/2023
 */

package com.nft.gallery.metaplex

import com.solana.core.PublicKey

/*
 * The Minty Fresh Creator PDA
 *
 * This PDA is added as an unverified creator (0 share) on all minty fresh NFTs so we can
 * efficiently find and filter these NFTs for display in the app
 */
fun MintyFreshCreatorPda(userPublicKey: PublicKey) =
    PublicKey.findProgramAddress(
        listOf("Minty Fresh".toByteArray()),
        userPublicKey
    ).address