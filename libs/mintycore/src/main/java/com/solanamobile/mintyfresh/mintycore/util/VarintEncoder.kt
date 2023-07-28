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

    fun decode(bytes: ByteArray): Long =
        bytes.takeWhile { it and 0x80.toByte() < 0 }.run {
            this + bytes[this.size]
        }.foldIndexed(0L) { index, value, byte ->
            ((byte and 0x7f).toLong() shl (7*index)) or value
        }
    fun decode(bytes: ByteBuffer): Int {
        var value = 0
        var shift = 0
        var b: Int
        do {
            b = bytes.get().toInt() and 0xFF
            value = value or ((b and 0x7F) shl shift)
            shift += 7
        } while (b and 0x80 != 0)
        return value
    }
}

fun Int.asVarint() = Varint.encode(this)
fun Long.asVarint() = Varint.encode(this)
fun ByteBuffer.decodeVarint() = Varint.decode(this)