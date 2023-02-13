package com.solanamobile.mintyfresh.networkinterface.pda

import com.solanamobile.mintyfresh.networkinterface.BuildConfig

/**
 * The Minty Fresh Creator PubKey.
 *
 * This PubKey is added as an unverified creator (0 share) on all minty fresh NFTs so we can
 * efficiently find and filter these NFTs for display in the app
 */
const val mintyFreshCreatorPubKey = BuildConfig.MINTY_FRESH_CREATOR_PUBKEY