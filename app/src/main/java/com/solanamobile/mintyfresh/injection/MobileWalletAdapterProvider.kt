package com.solanamobile.mintyfresh.injection

import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter

interface MobileWalletAdapterProvider {
    val mobileWalletAdapter: MobileWalletAdapter
}