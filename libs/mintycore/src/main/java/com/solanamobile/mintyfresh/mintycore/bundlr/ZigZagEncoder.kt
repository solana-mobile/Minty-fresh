package com.solanamobile.mintyfresh.mintycore.bundlr

object ZigZagEncoder {

    fun encode(value: Int): Int {
        var zigzag = value shl 1
        if (zigzag < 0) zigzag = zigzag.inv()
        return zigzag
    }

    fun encode(value: Long): Long {
        var zigzag = value shl 1
        if (zigzag < 0) zigzag = zigzag.inv()
        return zigzag
    }
}

fun Int.zigzag() = ZigZagEncoder.encode(this)
fun Long.zigzag() = ZigZagEncoder.encode(this)