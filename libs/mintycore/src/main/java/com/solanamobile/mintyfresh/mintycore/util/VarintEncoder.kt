package com.solanamobile.mintyfresh.mintycore.util

import java.nio.ByteBuffer
import kotlin.experimental.and
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

    fun encode(value: Long): ByteArray {
        var num = value
        val encodedSize = ceil((Long.SIZE_BITS - value.countLeadingZeroBits()) / 7f).toInt()
        return ByteArray(encodedSize) {
            (num and 0x7F or if (num < 128) 0 else 128).toByte().also {
                num /= 128
            }
        }
    }

//    fun decode(bytes: ByteArray): Int =
//        bytes.takeWhile { it and 0x80.toByte() > 0 }
//            .foldIndexed(0) { index, value, byte ->
//                (byte.toInt() shl (7*index)) or value
//            }
    fun decode(bytes: ByteArray): Long =
        bytes.takeWhile { it and 0x80.toByte() < 0 }.run {
            this + bytes[this.size]
        }.foldIndexed(0L) { index, value, byte ->
            ((byte and 0x7f).toLong() shl (7*index)) or value
        }
//            = bytes.foldIndexed(0L) { index, value, byte ->
//                ((byte and 0x7f).toLong() shl (7*index)) or value
//            }
}

fun Int.asVarint() = Varint.encode(this)
fun Long.asVarint() = Varint.encode(this)