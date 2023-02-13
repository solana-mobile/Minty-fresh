package com.solanamobile.mintyfresh.mintycore.ipld

import kotlin.math.ceil

object Varint {

    fun encode(value: Int): ByteArray {
        var num = value
        val encodedSize = ceil((Int.SIZE_BITS - value.countLeadingZeroBits()) / 7f).toInt()
        return ByteArray(encodedSize) {
            (num and 0x7F or if (num < 128) 0 else 128).toByte().also {
                num /= 128
            }
        }
    }
}

fun Int.asVarint() = Varint.encode(this)