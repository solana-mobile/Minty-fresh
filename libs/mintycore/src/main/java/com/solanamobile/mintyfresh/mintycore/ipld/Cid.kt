package com.solanamobile.mintyfresh.mintycore.ipld

import io.ipfs.multibase.Multibase

data class CID(val bytes: ByteArray)

fun CID.toCanonicalString(): String = Multibase.encode(Multibase.Base.Base32, bytes)