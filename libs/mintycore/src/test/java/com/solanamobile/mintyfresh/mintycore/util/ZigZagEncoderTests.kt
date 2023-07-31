package com.solanamobile.mintyfresh.mintycore.util

import org.junit.Assert
import org.junit.Test

class ZigZagEncoderTests {

    @Test
    fun testZigzagEncode() {
        // given
        val value = 2147483647L
        val expected = 4294967294L

        // when
        val result = value.zigzag()

        // then
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testZigzagDecode() {
        // given
        val value = 4294967294L
        val expected = 2147483647L

        // when
        val result = value.zigzagToTwosComp()

        // then
        Assert.assertEquals(expected, result)
    }

    @Test
    fun testZigzagEncodeDecode() {
        // given
        val value = 583578239L

        // when
        val zigzag = value.zigzag()
        val twosComp = zigzag.zigzagToTwosComp()

        // then
        Assert.assertEquals(value, twosComp)
    }
}