package com.solanamobile.mintyfresh.mintycore.util

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

    fun decode(value: Int): Int {
        var zigzag = value
        if (zigzag and 1 > 0) zigzag = zigzag.inv()
        return zigzag shr 1
    }

    fun decode(value: Long): Long {
        var zigzag = value
        if (zigzag and 1 > 0) zigzag = zigzag.inv()
        return zigzag shr 1
    }
}

fun Int.zigzag() = ZigZagEncoder.encode(this)
fun Long.zigzag() = ZigZagEncoder.encode(this)

fun Int.zigzagToTwosComp() = ZigZagEncoder.decode(this)
fun Long.zigzagToTwosComp() = ZigZagEncoder.decode(this)