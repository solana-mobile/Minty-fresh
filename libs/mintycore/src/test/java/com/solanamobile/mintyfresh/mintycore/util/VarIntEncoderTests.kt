package com.solanamobile.mintyfresh.mintycore.util

import org.junit.Assert
import org.junit.Test

class VarIntEncoderTests {
    @Test
    fun testVarintEncode() {
        // given
        val value = 16385
        val expected = byteArrayOf(-127, -128, 1)

        // when
        val result = Varint.encode(value)

        // then
        Assert.assertArrayEquals(expected, result)
    }

    @Test
    fun testVarintDecode() {
        // given
        val bytes = byteArrayOf(-127, -128, 1)
        val expected = 16385L

        // when
        val result = Varint.decode(bytes)

        // then
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testVarintEncodeDecode() {
        // given
        val value = 375847L

        // when
        val encoded = Varint.encode(value)
        val decoded = Varint.decode(encoded)

        // then
        Assert.assertEquals(value, decoded)
    }
}