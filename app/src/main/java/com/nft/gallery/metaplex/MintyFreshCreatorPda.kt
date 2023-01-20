package com.nft.gallery.metaplex

import com.nft.gallery.BuildConfig
import com.solana.core.PublicKey

/*
 * The Minty Fresh Creator PDA
 *
 * This PDA is added as an unverified creator (0 share) on all minty fresh NFTs so we can
 * efficiently find and filter these NFTs for display in the app
 */
val mintyFreshCreatorPda = PublicKey(BuildConfig.MINTY_FRESH_CREATOR_PDA)